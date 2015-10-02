package org.di.engine

import org.di.api.ProjectSource
import org.di.api.Version
import org.di.graph.Edge

class GradualDependencyIncrementer extends DependencyIncrementer {
    Map<Edge, Version> originalVersions = [:]
    Map<Edge, List<Version>> failingVersions = [:].withDefault {[]}
    Map<Edge, List<Version>> passingVersions = [:].withDefault {[]}

    @Override
    boolean increment() {
        boolean result = false
        Edge staleEdge = node.outgoing.find { it.isStale() }

        if (staleEdge != null) {
            if (passingVersions[staleEdge].size() == 0) { //assume initial version is passing so it can be rolled back
                passingVersions[staleEdge] << staleEdge.dependency.version
            }
            if (originalVersions[staleEdge] != null) {
                passingVersions[staleEdge] << originalVersions[staleEdge]
            }
            originalVersions[staleEdge] = staleEdge.dependency.version
            Version nextVersion = getNextVersion(staleEdge.to.projectSource, max(lastOrNull(passingVersions[staleEdge]), lastOrNull(failingVersions[staleEdge])))
            if (nextVersion == null) {
                result = false
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
        if (currentIndex == projectSource.versions.size() -1) {
          return null
        }
        return projectSource.versions[currentIndex+1]
    }

    Version max(Version first, second) {
        Version result
        if (first == null) {
            result = second
        } else
        if (second == null) {
            result = first
        } else
        if (first.after(second)) {
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
