package org.di.engine

import org.di.api.ProjectSource
import org.di.graph.Graph
import org.di.graph.Node

class SpanningTreeBuilder {
    Graph world
    String treeRoot


    Collection<Node> getConnectedProjects() {
        Node rootNode = world.nodes.find{it.name == treeRoot}
        if (!rootNode) {
            return []
        }
        def stack = [rootNode]
        Set<Node> foundNodes = new HashSet<Node>()
        foundNodes << rootNode

        while(!stack.isEmpty()){
            getDependencies(stack.pop(), stack, foundNodes)
        }
        return foundNodes
    }

    void getDependencies(Node visiting, List stack, Set visitedNodes){
        world.nodes.findAll{it.outgoing.find {it.to == visiting}}.each {
            if (visitedNodes.add(it)) {
                stack.push(it)
            }
        }
    }
}
