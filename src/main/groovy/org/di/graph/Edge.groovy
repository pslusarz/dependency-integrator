package org.di.graph

import org.di.api.Dependency
import org.di.api.Version

class Edge {
    Node to, from
    Dependency dependency
    boolean updateFailed = false
    boolean cyclic = false
    boolean isStale() {
        dependency.version.toString() != to.projectSource.latestVersion.toString()
    }

    def setDependencyVersion(Version version) {
        from.projectSource.setDependencyVersion(dependency, version)
        dependency = from.projectSource.dependencies.find {it.projectSourceName == dependency.projectSourceName}

    }
}
