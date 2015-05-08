package org.di.api.impl.carfax

import org.di.api.ProjectSource
import org.di.api.SourceRepository
import org.di.engine.BuildRecord
import org.di.engine.BuildRunner
import org.di.graph.Graph
import org.di.graph.visualization.GraphVizGenerator


public class Main {
    public static void main(String... args) {
        SourceRepository repository = new CarfaxLibSourceRepository(localDir: new File("D:/hackathon"));
        // repository.downloadAll()
        //drawGraph(repository)
        def projects = repository.init()
        updateLevel(repository, 2)
        //buildAll(projects)


    }

    static updateLevel(SourceRepository repository, int rank) {
        Graph g = new Graph(repository)
        g.initRank()
        g.nodes.findAll {it.rank == rank} {

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
