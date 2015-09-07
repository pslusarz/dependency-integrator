package org.di.engine

import org.di.api.ProjectSource
import org.di.graph.Edge
import org.di.graph.Graph
import org.di.graph.Node


class StalenessCalculator {
    private Graph graph

    def StalenessCalculator(Graph graph) {
        this.graph = graph
    }

    int getMetric() {
        int result = 0
        graph.nodes.each { Node node ->
          node.outgoing.each {Edge edge ->
            if (edge.isStale()) {
                ProjectSource projectSource = edge.to.projectSource
                //println "edge dependency: "+edge.dependency.version
                //println "projectsource versions: "+projectSource.versions
                int staleness = projectSource.versions.reverse().indexOf(edge.dependency.version)
                result += staleness
            }
          }
        }
        return result
    }
}
