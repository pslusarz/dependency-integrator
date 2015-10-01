package org.di.engine

import groovy.util.logging.Log
import org.di.api.ProjectSource
import org.di.api.SourceRepository
import org.di.graph.Edge
import org.di.graph.Graph
import org.di.graph.Node

@Log
class Updater {
    Graph graph
    int numberOfThreads = 4
    int numberOfBuildsPerformed = 0
    public Updater(Graph graph) {
        graph.initRank()
        this.graph = graph
    }
    public Updater(SourceRepository repository) {
        this(new Graph(repository))
    }

    def update() {
        (2..graph.maxRank).each {
            updateRank(it)
        }
    }

    def updateRank(int rank) {
        log.info "===== Updating rank "+rank+" ====="
        Map<ProjectSource, BulkDependencyIncrementer> updates = [:]

        graph.initRank()
        graph.nodes.findAll { it.rank == rank && it.outgoing.find { it.isStale() } }.each { Node node ->
            println node.projectSource.name
            node.outgoing.findAll { it.stale }.each { Edge edge ->
                println "   " + edge.dependency.projectSourceName + "  " + edge.dependency.version + " (" + edge.to.projectSource.latestVersion + ")"

            }
            def update = new BulkDependencyIncrementer(node: node)
            updates[node.projectSource] = update
        }
        BuildRunner br = new BuildRunner(projectSources: updates.keySet())
        br.start(numberOfThreads)
        List<BuildRecord> results = br.completeBuildRecords
        numberOfBuildsPerformed += results.size()
        def failedBeforeUpdate = results.findAll { it.result == BuildRecord.BuildResult.Failed }.collect {
            it.projectSource
        }

        graph.tagNodes(failedBeforeUpdate, 'failedBeforeUpdate')
        log.info "    Failed before update: " + failedBeforeUpdate
        Map<ProjectSource, BulkDependencyIncrementer> candidates = updates.findAll { true}
//            !failedBeforeUpdate.contains(it.key)
//        }
        candidates.each {
            it.value.increment()
        }


        def failedAfterUpdate = candidates.keySet()
        (1..4).each {
            if (failedAfterUpdate.size() > 0) {
                BuildRunner br2 = new BuildRunner(projectSources: failedAfterUpdate)
                br2.start(numberOfThreads)
                List<BuildRecord> resultsAfterUpgrade = br2.completeBuildRecords
                failedAfterUpdate = resultsAfterUpgrade.findAll {
                    it.result == BuildRecord.BuildResult.Failed
                }.collect {
                    it.projectSource
                }
            }
        }
        log.info "    Failed after update: " + failedAfterUpdate
        failedAfterUpdate.each { failed ->
            updates[failed].rollback()
        }
        graph.tagNodes(failedAfterUpdate, 'failedAfterUpdate')


        def successfulUpdate = candidates.keySet()
        successfulUpdate.removeAll(failedAfterUpdate)
//                resultsAfterUpgrade.findAll { it.result == BuildRecord.BuildResult.Passed }.collect {
//            it.projectSource
//        }

        successfulUpdate.each { successful ->
           // println successful.class
            successful.incrementVersion()
            successful.publishArtifactToTestRepo()
        }
        log.info "    Update worked: "+successfulUpdate
        graph.tagNodes(successfulUpdate, 'updateWorked')
        log.info "===== Done updating rank "+rank+" ====="
        graph = graph.rebuild()
        graph.initRank()
    }
}
