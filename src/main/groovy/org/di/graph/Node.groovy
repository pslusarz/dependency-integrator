package org.di.graph

import groovy.transform.ToString
import org.di.api.ProjectSource

class Node {
    int rank = -1
    ProjectSource projectSource
    List<Edge> outgoing = []

    String getName() {
        projectSource?.name
    }

    @Override
    String toString() {
        return name
    }

}
