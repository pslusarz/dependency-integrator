package org.di.graph

import org.di.api.SourceRepository
import org.di.api.impl.utils.SourceRepositoryForTesting
import org.junit.Test

class GraphRankAssignmentTest {
   @Test
    void testSingeNodeGetsARank() {
       SourceRepository sr = new SourceRepositoryForTesting({
           project {
               name = "lonely"
           }
       })

       Graph g = new Graph(sr)
       assert g.nodes[0].rank == -1
       g.initRank()
       assert g.nodes[0].rank == 1
   }

    @Test
    void testLadder() {
        SourceRepository sr = new SourceRepositoryForTesting({
            project {
                name = "primus"
                depends ("secundus")
            }

            project {
                name = "secundus"
                depends ("tertius")
            }

            project {
                name = "tertius"
            }
        })

        Graph g = new Graph(sr)

        g.initRank()
        assert g.nodes.find {it.name == "primus"}.rank == 3
        assert g.nodes.find {it.name == "secundus"}.rank == 2
        assert g.nodes.find {it.name == "tertius"}.rank == 1
    }

    @Test
    void testRankDeterminedByHighestDependencyRank() {
        SourceRepository sr = new SourceRepositoryForTesting({
            project {
                name = "primus"
                depends "longinus"
                depends ("secundus")
            }

            project {
                name = "longinus"
            }

            project {
                name = "secundus"
                depends ("tertius")
            }

            project {
                name = "tertius"
            }
        })

        Graph g = new Graph(sr)

        g.initRank()
        assert g.nodes.find {it.name == "primus"}.rank == 3
    }
}
