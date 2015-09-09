package org.di.engine

import org.di.api.impl.utils.SourceRepositoryForTesting
import org.di.graph.Graph
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

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

    @Test
    void singleEdgeInRankedGraph() {
        Graph g = new Graph(new SourceRepositoryForTesting({
            project {
                name = "root"
                version = 6
                versions = [1,2,3,4,5,6]
            }
            project {
                name = "child"
                depends("root")
            }

            project {
                name = "grandchild"
                depends("child")
            }

        }))
        assert 10 == new StalenessCalculator(g).metric
    }

    @Test
    void singleEdgeInterconnectedDiamond() {
        Graph g = new Graph(new SourceRepositoryForTesting({
            project {
                name = "root"
                version = 6
                versions = [1,2,3,4,5,6]
            }
            project {
                name = "diamond-bottom"
                depends("root", 1)
            }

            project {
                name = "diamond-left"
                depends("diamond-bottom")
            }
            project {
                name = "diamond-right"
                depends("diamond-bottom")
            }

            project {
                name = "diamond-top"
                depends("diamond-left")
                depends("diamond-right")
                depends("diamond-bottom")
            }

        }))
        assert 30 == new StalenessCalculator(g).metric
    }

    @Test
    void isolatedSubtrees() {
        Graph g = new Graph(new SourceRepositoryForTesting({
            project {
                name = "root"
                version = 3
                versions = [1,2,3]
            }
            project {
                name = "left"
                depends("root", 1)
            }

            project {
                name = "right"
                depends("root", 3)
            }
        }))
        assert 2 == new StalenessCalculator(g).metric
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    void throwExceptionIfCannotFindReferencedVersion() {
        Graph g = new Graph(new SourceRepositoryForTesting({
            project {
                name = "root"
                version = 3
                versions = [1,3]
            }
            project {
                name = "child"
                depends("root", 2)
            }

        }))
        thrown.expect(RuntimeException)
        thrown.expectMessage("Cannot find version VersionForTesting(2)")
        thrown.expectMessage(" for project root")
        thrown.expectMessage(" among versions [VersionForTesting(1), VersionForTesting(3)]")
        thrown.expectMessage(" required by project child")
        new StalenessCalculator(g).metric
    }

    //Cases from blog: http://10kftcode.blogspot.com/2015/08/measuring-staleness-advanced-dependency.html

    @Test
    void firstExample() {
        Graph g = new Graph(new SourceRepositoryForTesting({
            project {
                name = "10"
                version = 8
                versions = [1,2,3,4,5,6,7,8]
            }
            project {
                name = "20"
                depends("10", 3)
            }

            project {
                name = "21"
                depends("10", 1)
            }

            project {
                name = "30"
                depends("20")
            }

            project {
                name = "31"
                depends("20")
                depends("21")
            }
        }))
        assert 29 == new StalenessCalculator(g).metric
    }

    @Test
    void secondExamplePreIntegration() {
        Graph g = new Graph(new SourceRepositoryForTesting({
            project {
                name = "10"
                version = 5
                versions = [1,2,3,4,5]
            }
            project {
                name = "20"
                version = 6
                versions = [1,2,3,4,5,6]
                depends("10", 1)
            }

            project {
                name = "21"
                depends("10", 3)
            }

            project {
                name = "30"
                depends("20", 1)
            }

            project {
                name = "31"
                depends("20", 3)
                depends("21")
            }
        }))
        assert 24 == new StalenessCalculator(g).metric
    }

    @Test
    void secondExamplePostIntegration() {
        Graph g = new Graph(new SourceRepositoryForTesting({
            project {
                name = "10"
                version = 5
                versions = [1,2,3,4,5]
            }
            project {
                name = "20"
                version = 10
                versions = [1,2,3,4,5,6,7,8,9,10]
                depends("10", 5) //latest
            }

            project {
                name = "21"
                version = 2
                versions = [1,2]
                depends("10", 5) //latest
            }

            project {
                name = "30"
                depends("20", 5)
            }

            project {
                name = "31"
                depends("20", 9)
                depends("21",1)
            }
        }))
        assert 7 == new StalenessCalculator(g).metric
    }

    //TODO: ignore cycles


}
