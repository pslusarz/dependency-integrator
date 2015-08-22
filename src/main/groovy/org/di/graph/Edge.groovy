package org.di.graph

import org.di.api.Dependency

class Edge {
    Node to, from
    Dependency dependency
    boolean updateFailed = false
    boolean cyclic = false
    boolean isStale() {
        dependency.version.toString() != to.projectSource.latestVersion.toString()
    }
}
