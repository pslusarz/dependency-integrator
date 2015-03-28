package org.di.graph

import org.di.api.Dependency
import org.di.api.ProjectSource
import org.di.api.SourceRepository

class Graph {
    SourceRepository sourceRepository
    List<Node> nodes = []

    Graph(sourceRepository) {
        this.sourceRepository = sourceRepository
        sourceRepository.init().each { ProjectSource project ->
            nodes << new Node(projectSource: project)
        }
        nodes.each {Node node ->
            node.projectSource.dependencies.findAll{nodes.collect {it.name}.contains(it.projectSourceName)}.each {Dependency dependency ->
               node.outgoing << new Edge(to: nodes.find {it.name == dependency.projectSourceName})
            }
        }
    }
}
