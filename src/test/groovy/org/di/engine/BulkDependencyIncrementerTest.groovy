package org.di.engine

import org.di.api.ProjectSource
import org.di.api.SourceRepository
import org.di.api.impl.utils.ProjectSourceForTesting
import org.di.api.impl.utils.SourceRepositoryForTesting
import org.di.api.impl.utils.VersionForTesting
import org.di.graph.Graph
import org.junit.Test

class BulkDependencyIncrementerTest {
    @Test
    void testSingleDep() {
        Graph g = new Graph(new SourceRepositoryForTesting({
            project {
                name = "one"
                depends("two", 1)
            }
            project {
                name = "two"
                version = 2
            }
        }))

        def incremented = new BulkDependencyIncrementer(node: g.node("one")).increment()
        assert incremented
        assert g.rebuild().node("one").outgoing("two").dependency.version == new VersionForTesting(value: 2)
    }

    @Test
    void testHandleCurrentDependency() {
        Graph g = new Graph(new SourceRepositoryForTesting({
            project {
                name = "one"
                depends ("current", 2)
                depends ("stale", 2)
            }
            project {
                name = "current"
                version = 2
            }
            project {
                name = "stale"
                version = 3
            }
        }))
        def di = new BulkDependencyIncrementer(node: g.node("one"))
        di.increment()
        def currentDependency = g.rebuild().node("one").outgoing ("current").dependency
        def staleDependency =   g.rebuild().node("one").outgoing("stale")  .dependency
        assert currentDependency.version == new VersionForTesting(value: 2)
        assert staleDependency.version == new VersionForTesting(value: 3)

    }

    @Test
    void testNothingToIncrement() {
        Graph g = new Graph(new SourceRepositoryForTesting({
            project {
                name = "one"
                depends ("current", 2)
            }
            project {
                name = "current"
                version = 2
            }
        }))
        def di = new BulkDependencyIncrementer(node: g.node("one"))
        def incremented = di.increment()
        assert !incremented
        def currentDependency = g.rebuild().node("one").outgoing("current").dependency
        assert currentDependency.version == new VersionForTesting(value: 2)
    }

    @Test
    void testRollback() {
        Graph g = new Graph(new SourceRepositoryForTesting({
            project {
                name = "one"
                depends ("current", 3)
                depends ("stale", 1)
            }
            project {
                name = "current"
                version = 3
            }
            project {
                name = "stale"
                version = 2
            }
        }))
        def di = new BulkDependencyIncrementer(node: g.node("one"))
        di.increment()
        di.rollback()

        def rolledbackDependency = g.rebuild().node("one").outgoing("stale").dependency
        def untouchedDependency = g.rebuild().node("one").outgoing("current").dependency


        assert rolledbackDependency.version == new VersionForTesting(value: 1)
        assert rolledbackDependency.guarded
        assert untouchedDependency.version == new VersionForTesting(value: 3)
        assert !untouchedDependency.guarded

    }
}
