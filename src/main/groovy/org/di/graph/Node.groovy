package org.di.graph

import groovy.transform.ToString
import org.di.api.ProjectSource

class Node {
    int rank = -1
    boolean buildFailed = false
    Map tags = [:]
    ProjectSource projectSource
    List<Edge> outgoing = []

    String getName() {
        projectSource?.name
    }

    Edge outgoing(String projectSourceName) {
        outgoing.find { it.to.name == projectSourceName }
    }

    @Override
    String toString() {
        return name
    }

}
