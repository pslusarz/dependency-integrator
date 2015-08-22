package org.di.engine

import org.di.api.Dependency
import org.di.api.ProjectSource
import org.di.api.Version
import org.di.graph.Edge
import org.di.graph.Node

class BulkDependencyIncrementer {
    ProjectSource projectSource
    Collection<ProjectSource> projectSources
    Map<Dependency, Version> originalVersions = [:]
    Node node

    boolean increment() {
        boolean result = false
        node.outgoing.each { Edge edge ->
            if (edge.isStale()) {
                originalVersions[edge.dependency] = edge.dependency.version
                node.projectSource.setDependencyVersion(edge.dependency, edge.to.projectSource.latestVersion)
                edge.dependency = node.projectSource.dependencies.find {it.projectSourceName == edge.dependency.projectSourceName} //todo - test
                result = true    //todo: test me!

            }
        }
        return result
    }

    void rollback() {
        originalVersions.each { Dependency dependency, Version originalVersion ->
            node.projectSource.setDependencyVersion(dependency, originalVersion)
            node.projectSource.setDependencyGuarded(dependency)
            //todo : test me!
            node.outgoing.find {it.dependency.projectSourceName == dependency.projectSourceName}.dependency = node.projectSource.dependencies.find {it.projectSourceName == dependency.projectSourceName}
        }
    }

}
