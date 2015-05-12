package org.di.api.impl.carfax

import org.di.api.ProjectSource
import org.di.api.SourceRepository
import org.di.engine.BuildRecord
import org.di.engine.BuildRunner
import org.di.engine.BulkDependencyIncrementer
import org.di.graph.Edge
import org.di.graph.Graph
import org.di.graph.Node
import org.di.graph.visualization.GraphVizGenerator


public class Main {
    public static void main(String... args) {
        SourceRepository repository = new CarfaxLibSourceRepository(localDir: new File("D:/hackathon"));
        // repository.downloadAll()
        //drawGraph(repository)
        def projects = repository.init()
        //updateOne("carfax-websidestory", projects)
        updateLevel(repository, 2)
        //buildAll(projects)


    }

    static updateOne(String projectName, Collection<ProjectSource> projects) {
        BulkDependencyIncrementer b = new BulkDependencyIncrementer(projectSource: projects.find {it.name == projectName}, projectSources:  projects)
        b.increment()
    }

    static updateLevel(SourceRepository repository, int rank) {
        Map<ProjectSource, BulkDependencyIncrementer> updates = [:]
        Graph g = new Graph(repository)
        g.initRank()
        g.nodes.findAll {it.rank == rank && it.outgoing.find{it.isStale()}}.each { Node node ->
           println node.projectSource.name
           node.outgoing.findAll {it.stale}.each { Edge edge ->
               println "   "+edge.dependency.projectSourceName + "  "+edge.dependency.version+" ("+edge.to.projectSource.version+")"

           }
           def update = new BulkDependencyIncrementer(node: node)
           updates[node.projectSource] = update
        }
        BuildRunner br = new BuildRunner(projectSources: updates.keySet())
        br.start(4)
        List<BuildRecord> results = br.completeBuildRecords
        def failedBeforeUpdate = results.findAll {it.result == BuildRecord.BuildResult.Failed}.collect {it.projectSource}
        println "Failed before upgrade: "+failedBeforeUpdate
        Map<ProjectSource, BulkDependencyIncrementer> candidates = updates.findAll {!failedBeforeUpdate.contains(it.key)}
        candidates.each {
           it.value.increment()
        }
        BuildRunner br2 = new BuildRunner(projectSources: candidates.keySet())
        br2.start(4)
        List<BuildRecord> resultsAfterUpgrade = br2.completeBuildRecords
        def failedAfterUpdate = resultsAfterUpgrade.findAll {it.result == BuildRecord.BuildResult.Failed}.collect {it.projectSource}
        println "Failed after update: "+failedAfterUpdate
        failedAfterUpdate.each {
            updates[it].rollback()
        }
    }

    static buildAll(projects) {
        long start = System.currentTimeMillis()
        BuildRunner br = new BuildRunner(projectSources: projects)
        br.start(8)
        def results = br.completeBuildRecords
        long stop = System.currentTimeMillis()
        results.findAll { it.result == BuildRecord.BuildResult.Failed }.each {
            println it.projectSource.name + " " + it.result
        }

        println "Total time (ms): " + (stop - start)
    }

    static drawGraph(repository) {
        Graph g = new Graph(repository)
        g.initRank()
        g.cycles.each {
            println it
        }
        def gv = new GraphVizGenerator(graph: g)
        gv.generate()
        gv.reveal()
    }


}
