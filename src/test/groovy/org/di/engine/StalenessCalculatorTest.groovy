package org.di.engine

import org.di.api.impl.utils.SourceRepositoryForTesting
import org.di.graph.Graph
import org.junit.Test

class StalenessCalculatorTest {

    @Test
    void singleNode() {
        Graph g = new Graph(new SourceRepositoryForTesting({ project { name = "root" } }))
        assert 0 == new StalenessCalculator(g).metric
    }

    @Test
    void twoNodesUpToDate() {
        Graph g = new Graph(new SourceRepositoryForTesting({
            project { name = "root" }
            project {
                name = "child"
                depends "root"
            }

        }))
        assert 0 == new StalenessCalculator(g).metric
    }

    @Test
    void oneSimplest() {
        Graph g = new Graph(new SourceRepositoryForTesting({
            project {
                name = "root"
                version = 2
                versions = [1,2]
            }
            project {
                name = "child"
                depends("root", 1)
            }

        }))
        assert 1 == new StalenessCalculator(g).metric
    }

    @Test
    void singleEdgeMultipleVersionsBehind() {
        Graph g = new Graph(new SourceRepositoryForTesting({
            project {
                name = "root"
                version = 3
                versions = [1,2,3]
            }
            project {
                name = "child"
                depends("root", 1)
            }

        }))
        assert 2 == new StalenessCalculator(g).metric

    }

    //TODO: ignore cycles


}
