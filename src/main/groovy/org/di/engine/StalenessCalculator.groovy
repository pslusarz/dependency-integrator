package org.di.engine

import groovy.util.logging.Log
import org.di.api.ProjectSource
import org.di.graph.Edge
import org.di.graph.Graph
import org.di.graph.Node

@Log
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
                SpanningTreeBuilder spanningTreeBuilder = new SpanningTreeBuilder(world: graph, treeRoot: node.name)
                def edges = getConnectingEdges(spanningTreeBuilder.connectedProjects)
                int reachableEdges = edges.size() + 1 //add current stale edge to the calculation
                if (staleness == -1) {
                    staleness = projectSource.versions.size() -1
                    //throw new RuntimeException("Cannot find version "+edge.dependency.version.toString()+" for project "+projectSource.name+" among versions "+projectSource.versions+" required by project "+node.name)
                    log.warning("Cannot find version "+edge.dependency.version.toString()+" for project "+projectSource.name+" among versions "+projectSource.versions+" required by project "+node.name)
                }
                result += staleness * reachableEdges
            }
          }
        }
        return result
    }

    private List<Edge> getConnectingEdges(Collection<Node> treeNodes) {
      treeNodes.collect { it.outgoing.findAll{treeNodes.contains(it.to)}}.flatten()
    }
}
