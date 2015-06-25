package org.di.api.impl.carfax

import org.di.api.ProjectSource
import org.di.api.SourceRepository
import org.di.engine.BuildRecord
import org.di.engine.BuildRunner
import org.di.engine.BulkDependencyIncrementer
import org.di.engine.SpanningTreeBuilder
import org.di.graph.Edge
import org.di.graph.Graph
import org.di.graph.Node
import org.di.graph.visualization.GraphVizGenerator


public class Main {
    public static void main(String... args) {
        SourceRepository repository = new CarfaxLibSourceRepository(localDir: new File("D:/hackathon"));

        blah(repository)
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



    static blah(SourceRepository repository) {
        Graph g = new Graph(repository)
        Graph dependents = new Graph(new SpanningTreeBuilder(world: g, treeRoot: "dealerautoreports-commons").connectedProjects)
        dependents.initRank()

        BuildRunner br = new BuildRunner(projectSources: dependents.nodes.collect {it.projectSource} )
        br.start(4)
        def results = br.completeBuildRecords
        results.findAll { it.result == BuildRecord.BuildResult.Failed }.each { currentBuild ->
            dependents.nodes.find {it.name == currentBuild.projectSource.name }.buildFailed = true
        }
        def gv = new GraphVizGenerator(graph: dependents)
        gv.generate()
        gv.reveal()


//        buildLevelsGraph.nodes.findAll {it.rank == 7}.each { Node current ->
//            println current.name +"  "+ current.outgoing.size() + "  "+buildLevelsGraph.nodes.findAll {it.outgoing.find {it.to == current}}.size()
//        }

//        def gv = new GraphVizGenerator(graph: dependents)
//        gv.generate()
//        gv.reveal()

//        def nextLevel = dependents.findAll{it.rank == toBeIntegrated.rank + 1}.findAll {it.outgoing.find {it.to == toBeIntegrated}.isStale()}
//        //nextLevel.each {println it.name + " " + it.outgoing.find {it.to == toBeIntegrated}.isStale()+ "  "+ it.outgoing.find {it.to == toBeIntegrated}.dependency.version}
//        //println toBeIntegrated.name+" current version is: "+toBeIntegrated.projectSource.version
//        BuildRunner br = new BuildRunner(projectSources:  nextLevel.collect {it.projectSource})
//        br.start(4)
//        def results = br.completeBuildRecords
//        results.findAll { it.result == BuildRecord.BuildResult.Failed }.each {
//            println it.projectSource.name + " " + it.result
//        }
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
