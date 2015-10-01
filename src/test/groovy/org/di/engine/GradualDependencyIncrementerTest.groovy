package org.di.engine

import org.di.api.impl.utils.SourceRepositoryForTesting
import org.di.api.impl.utils.VersionForTesting
import org.di.graph.Graph
import org.junit.Test

class GradualDependencyIncrementerTest {
    @Test
    void testSingleIncrement() {
        Graph g = new Graph(new SourceRepositoryForTesting({
            project {
                name = "one"
                depends ("current", 1)
            }
            project {
                name = "current"
                version = 2
            }
        }))
        def di = new GradualDependencyIncrementer(node: g.node("one"))
        def incremented1 = di.increment()
        assert incremented1
        def incremented2 = di.increment()
        assert !incremented2
        assert g.rebuild().node("one").outgoing("current").dependency.version == new VersionForTesting(value: 2)
    }

    @Test
    void testIncrementGradually() {
        Graph g = new Graph(new SourceRepositoryForTesting({
            project {
                name = "one"
                depends ("current", 1)
            }
            project {
                name = "current"
                version = 3
            }
        }))
        def di = new GradualDependencyIncrementer(node: g.node("one"))

        def incremented1 = di.increment()
        assert incremented1
        assert g.rebuild().node("one").outgoing("current").dependency.version == new VersionForTesting(value: 2)

        def incremented2 = di.increment()
        assert incremented2
        assert g.rebuild().node("one").outgoing("current").dependency.version == new VersionForTesting(value: 3)

        assert !di.increment()
    }

    @Test
    void testSimpleRollback() {
        Graph g = new Graph(new SourceRepositoryForTesting({
            project {
                name = "one"
                depends ("current", 1)
            }
            project {
                name = "current"
                version = 2
            }
        }))
        def di = new GradualDependencyIncrementer(node: g.node("one"))

        di.increment()
        di.rollback()

        assert g.rebuild().node("one").outgoing("current").dependency.version == new VersionForTesting(value: 1) //node
        assert g.rebuild().node("one").projectSource.dependencies.find {it.projectSourceName == "current"}.version == new VersionForTesting(value: 1) //project associated with node
    }

    @Test
    void testIncrementTwoDependenciesGradually() {
        Graph g = new Graph(new SourceRepositoryForTesting({
            project {
                name = "one"
                depends ("left", 1)
                depends ("right", 4)
            }
            project {
                name = "left"
                version = 2
            }

            project {
                name = "right"
                version = 5
            }
        }))
        def di = new GradualDependencyIncrementer(node: g.node("one"))

        di.increment()
        di.increment()

        assert g.rebuild().node("one").outgoing("left").dependency.version == new VersionForTesting(value: 2)
        assert g.rebuild().node("one").outgoing("right").dependency.version == new VersionForTesting(value: 5)
    }

    @Test
    void bypassFailedTwoDependencies() {
        Graph g = new Graph(new SourceRepositoryForTesting({
            project {
                name = "one"
                depends ("left", 1)
                depends ("right", 4)
            }
            project {
                name = "left"
                version = 2
            }

            project {
                name = "right"
                version = 5
            }
        }))
        def di = new GradualDependencyIncrementer(node: g.node("one"))

        di.increment()
        di.rollback()
        di.increment()

        assert g.rebuild().node("one").outgoing("left").dependency.version == new VersionForTesting(value: 1) //rolled back
        assert g.rebuild().node("one").outgoing("right").dependency.version == new VersionForTesting(value: 5)
    }

    @Test
    void bypassFailedSingleDependency() {
        Graph g = new Graph(new SourceRepositoryForTesting({
            project {
                name = "one"
                depends ("two", 1)
            }
            project {
                name = "two"
                version = 5
            }

        }))
        def di = new GradualDependencyIncrementer(node: g.node("one"))

        di.increment()
        di.rollback() //failed on version 2
        di.increment()

        g.rebuild().node("one").outgoing("two").dependency.version == new VersionForTesting(value: 3)

        di.increment()
        assert g.rebuild().node("one").outgoing("two").dependency.version == new VersionForTesting(value: 4)
        di.rollback() //failed on 4
        di.increment()
        assert g.rebuild().node("one").outgoing("two").dependency.version == new VersionForTesting(value: 5)

    }

    @Test
    void rollbackToLastPassingIfContinuesToFail() {
        Graph g = new Graph(new SourceRepositoryForTesting({
            project {
                name = "one"
                depends ("two", 1)
            }
            project {
                name = "two"
                version = 5
            }

        }))
        def di = new GradualDependencyIncrementer(node: g.node("one"))

        di.increment() //last passing version: 2
        di.increment() //try version 3
        di.rollback()  //3 fails
        di.increment() //now try 4
        di.rollback() //4 fails
        di.increment() //try 5
        assert g.rebuild().node("one").outgoing("two").dependency.version == new VersionForTesting(value: 5)
        di.rollback() //5 fails
        assert g.rebuild().node("one").outgoing("two").dependency.version == new VersionForTesting(value: 2)
    }

}
