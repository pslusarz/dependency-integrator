package org.di.engine

import org.di.api.ProjectSource
import org.di.api.Version
import org.di.graph.Edge

class GradualDependencyIncrementer extends DependencyIncrementer {
    Map<Edge, Version> originalVersions = [:]
    Map<Edge, List<Version>> failingVersions = [:].withDefault { [] }
    Map<Edge, List<Version>> passingVersions = [:].withDefault { [] }
    List<Edge> exhaustedEdges = []

    @Override
    boolean increment() {
        boolean result = false
        Edge staleEdge = node.outgoing.find { it.isStale() && !exhaustedEdges.contains(it)}

        if (staleEdge != null) {
            originalVersions[staleEdge] = staleEdge.dependency.version
            passingVersions[staleEdge] << originalVersions[staleEdge]

            Version nextVersion = getNextVersion(staleEdge.to.projectSource, max(lastOrNull(passingVersions[staleEdge]), lastOrNull(failingVersions[staleEdge])))

            if (nextVersion == null) {
                result = false
                exhaustedEdges << staleEdge
                result = increment() //this edge is done, is there another?
            } else {
                staleEdge.setDependencyVersion(nextVersion)
                result = true
            }
            return true
        }
        return result
    }

    @Override
    void rollback() {
        originalVersions.each { Edge edge, Version version ->
            failingVersions[edge] << edge.dependency.version
            edge.setDependencyVersion(passingVersions[edge].last())

        }
        originalVersions.clear()
    }

    Version getNextVersion(ProjectSource projectSource, Version current) {
        int currentIndex = projectSource.versions.indexOf(current)
        if (currentIndex == -1) {
            return projectSource.versions.first()
        }
        if (currentIndex == projectSource.versions.size() - 1) {
            return null
        }
        return projectSource.versions[currentIndex + 1]
    }

    Version max(Version first, second) {
        Version result
        if (first == null) {
            result = second
        } else if (second == null) {
            result = first
        } else if (first.after(second)) {
            result = first
        } else {
            result = second
        }
        return result
    }

    Version lastOrNull(List<Version> list) {
        if (list.size() == 0) {
            return null
        } else {
            return list.last()
        }
    }
}
