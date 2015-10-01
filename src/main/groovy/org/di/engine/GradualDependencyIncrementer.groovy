package org.di.engine

import org.di.graph.Edge

class GradualDependencyIncrementer extends DependencyIncrementer {
    @Override
    boolean increment() {
        Edge staleEdge = node.outgoing.find {it.isStale()}
        if (staleEdge != null) {
            staleEdge.setDependencyVersion(staleEdge.to.projectSource.versions[staleEdge.to.projectSource.versions.indexOf(staleEdge.dependency.version)+1])
            return true
        } else {
            return false
        }
    }

    @Override
    void rollback() {

    }
}
