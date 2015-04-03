package org.di.graph

import org.di.api.Dependency
import org.di.api.Version

class Edge {
    Node to
    Dependency dependency
    boolean cyclic = false
}
