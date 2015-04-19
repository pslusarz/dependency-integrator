package org.di.api.impl

import org.di.api.SourceRepository
import org.di.graph.Graph


public class Main {
    public static void main(String... args) {
        SourceRepository repository = new CarfaxLibSourceRepository(localDir: new File("D:/hackathon"));
//        repository.downloadAll()
//        Graph g = new Graph(repository)
//        g.initRank()
//        GraphVizGenerator.generate(g)

    }





}
