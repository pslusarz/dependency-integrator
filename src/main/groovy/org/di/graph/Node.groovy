package org.di.graph

import org.di.api.ProjectSource

class Node {
    ProjectSource projectSource
    List<Edge> outgoing = []
    String getName() {
        projectSource?.name
    }
}
