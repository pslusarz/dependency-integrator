package org.di.engine

import org.di.api.Version
import org.di.graph.Edge

class GradualDependencyIncrementer extends DependencyIncrementer {
    Map<Edge, Version> originalVersions = [:]

    @Override
    boolean increment() {
        Edge staleEdge = node.outgoing.find { it.isStale() }
        if (staleEdge != null) {
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
            edge.setDependencyVersion(version)
        }
    }
}
