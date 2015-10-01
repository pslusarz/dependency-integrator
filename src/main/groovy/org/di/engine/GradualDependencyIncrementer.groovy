package org.di.engine

import org.di.api.Version
import org.di.graph.Edge

class GradualDependencyIncrementer extends DependencyIncrementer {
    Map<Edge, Version> originalVersions = [:]
    Map<Edge, List<Version>> failingVersions = [:].withDefault {[]}
    Map<Edge, List<Version>> passingVersions = [:].withDefault {[]}

    @Override
    boolean increment() {
        Edge staleEdge = node.outgoing.find { it.isStale() }
        if (staleEdge != null) {
            if (originalVersions[staleEdge] != null) {
                passingVersions[staleEdge] << originalVersions[staleEdge]
            }
            originalVersions[staleEdge] = staleEdge.dependency.version
            staleEdge.setDependencyVersion(staleEdge.to.projectSource.versions[staleEdge.to.projectSource.versions.indexOf(staleEdge.dependency.version) + 1])
            return true
        } else {
            return false
        }
    }

    @Override
    void rollback() {
        originalVersions.each { Edge edge, Version version ->
            failingVersions[edge] << edge.dependency.version
            edge.setDependencyVersion(version)

        }
        originalVersions.clear()
    }
}
