package org.di.engine

import org.di.api.Dependency
import org.di.api.ProjectSource
import org.di.api.Version
import org.di.graph.Edge
import org.di.graph.Node

class BulkDependencyIncrementer extends DependencyIncrementer {
    ProjectSource projectSource
    Collection<ProjectSource> projectSources
    Map<Dependency, Version> originalVersions = [:]
    //Node node

    @Override
    boolean increment() {
        boolean result = false
        node.outgoing.each { Edge edge ->
            if (edge.isStale()) {
                originalVersions[edge.dependency] = edge.dependency.version
                edge.setDependencyVersion(edge.to.projectSource.latestVersion)
                result = true

            }
        }
        return result
    }

    @Override
    void rollback() {
        originalVersions.each { Dependency dependency, Version originalVersion ->
            node.outgoing.find {it.dependency.projectSourceName == dependency.projectSourceName}.setDependencyVersion(originalVersion)
            node.projectSource.setDependencyGuarded(dependency)
        }
    }

}
