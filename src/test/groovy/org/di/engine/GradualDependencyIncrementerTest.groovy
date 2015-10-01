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
        def di = new GradualDependencyIncrementer(node: g.nodes.find { it.name == "one" })
        def incremented1 = di.increment()
        assert incremented1
        def incremented2 = di.increment()
        assert !incremented2
        def currentDependency = g.rebuild().nodes.find{it.name == "one"}.outgoing.find{it.to.name == "current"}.dependency //projects.find {it.name == "one"}.dependencies.find{it.projectSourceName == "current"}
        assert currentDependency.version == new VersionForTesting(value: 2)
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
        def di = new GradualDependencyIncrementer(node: g.nodes.find { it.name == "one" })

        def incremented1 = di.increment()
        assert incremented1
        def currentDependency = g.rebuild().nodes.find{it.name == "one"}.outgoing.find{it.to.name == "current"}.dependency //projects.find {it.name == "one"}.dependencies.find{it.projectSourceName == "current"}
        assert currentDependency.version == new VersionForTesting(value: 2)

        def incremented2 = di.increment()
        assert incremented2
        currentDependency = g.rebuild().nodes.find{it.name == "one"}.outgoing.find{it.to.name == "current"}.dependency //projects.find {it.name == "one"}.dependencies.find{it.projectSourceName == "current"}
        assert currentDependency.version == new VersionForTesting(value: 3)

        assert !di.increment()
    }
}
