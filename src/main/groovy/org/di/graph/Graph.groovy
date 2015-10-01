package org.di.graph

import org.di.api.Dependency
import org.di.api.ProjectSource
import org.di.api.SourceRepository

class Graph {
    List<Node> nodes = []
    List<List<Node>> cycles
    boolean rankInit = false
    int maxRank = -1

    Graph() {}

    Graph(SourceRepository sourceRepository) {
        this(sourceRepository.init())
    }

    Graph(Collection<ProjectSource> projectSources) {
        projectSources.each { ProjectSource project ->
            nodes << new Node(projectSource: project)
        }
        nodes.each { Node node ->
            node.projectSource.dependencies.findAll {
                nodes.collect { it.name }.contains(it.projectSourceName)
            }.each { Dependency dependency ->
                node.outgoing << new Edge(to: this.node(dependency.projectSourceName), dependency: dependency, from: node)
            }
        }
    }

    /**
     * Used to reinitialize edges when dependencies change
     */
    Graph rebuild() {
        return new Graph(nodes.collect{it.projectSource})
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
                def cycle = current.path.drop(current.path.indexOf(current.node))
                cycles << cycle
                markEdgesCyclic(cycle)
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

    public node(String name) {
        nodes.find {it.name == name}
    }

    private static markEdgesCyclic(List<Node> cycle) {
        Node previous = null
        cycle.each { Node node ->
            if (previous) {
                previous.outgoing.find { it.to == node }.cyclic = true
            }
            previous = node
        }
        cycle.last().outgoing.find { it.to == cycle.first() }.cyclic = true
    }

    def initRank() {
        if (rankInit) {
            return
        }
        if (cycles == null) {
            initCycles()
        }
        def changed = true
        while (changed) {
            changed = false
            nodes.each {
                if (it.outgoing.findAll{!it.cyclic}.size() == 0) {
                    if (it.rank == -1) {
                        it.rank = 1
                        changed = true
                    }
                } else if (!it.outgoing.findAll{!it.cyclic}.findAll { it.to.rank == -1 }) {
                    def newRank = (it.outgoing.findAll{!it.cyclic}.collect { it.to }.max { a, b -> a.rank <=> b.rank }).rank + 1
                    if (newRank != it.rank) {
                        changed = true
                        it.rank = newRank
                    }
                }
            }
        }

        maxRank = nodes.max {it.rank}.rank

    }

    public tagNodes(Collection<ProjectSource> projectSourcesToMatch, String tag) {

    }
}
