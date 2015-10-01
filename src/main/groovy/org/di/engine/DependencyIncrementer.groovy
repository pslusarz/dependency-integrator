package org.di.engine
import org.di.graph.Node

public abstract class DependencyIncrementer {
    Node node
    public abstract boolean increment()
    public abstract void rollback()

}