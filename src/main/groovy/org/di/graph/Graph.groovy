package org.di.graph

import org.di.api.Dependency
import org.di.api.ProjectSource
import org.di.api.SourceRepository

class Graph {
    SourceRepository sourceRepository
    List<Node> nodes = []
    List<List<Node>> cycles

    Graph(sourceRepository) {
        this.sourceRepository = sourceRepository
        sourceRepository.init().each { ProjectSource project ->
            nodes << new Node(projectSource: project)
        }
        nodes.each { Node node ->
            node.projectSource.dependencies.findAll {
                nodes.collect { it.name }.contains(it.projectSourceName)
            }.each { Dependency dependency ->
                node.outgoing << new Edge(to: nodes.find { it.name == dependency.projectSourceName })
            }
        }
    }

    def initCycles() {
        cycles = []
        def stack = []
        HashSet<Node> visited = new HashSet()
        nodes.each {
            stack.push([node: it, path: []])
        }
        while (!stack.isEmpty()) {
            def current = stack.pop()
            if (current.path.collect { it.name }.contains(current.node.name)) {
                cycles << current.path.drop(current.path.indexOf(current.node))
                // a, b, c, b, c... -> 'a' is not part of the cycle
            } else if (!visited.contains(current.node)) {
                current.node.outgoing.collect { it.to }.each { Node dependency ->
                    def newPath = []
                    newPath.addAll(current.path)
                    newPath << current.node
                    stack.push([node: dependency, path: newPath])
                }
                visited << current.node
            }


        }

    }

    def initRank() {
//        if (!cycles) {
//            initCycles()
//        }
        def changed = true
        while (changed) {
            changed = false
            nodes.each {
                if (it.outgoing.size() == 0) {
                    if (it.rank == -1) {
                        it.rank = 1
                        changed = true
                    }
                } else if (!it.outgoing.findAll { it.to.rank == -1 }) {
                    def newRank = (it.outgoing.collect{it.to}.max { a, b -> a.rank <=> b.rank }).rank + 1
                    if (newRank != it.rank) {
                        changed = true
                        it.rank = newRank
                    }
                }
            }
        }

    }
}
