package org.di.graph

import org.di.api.SourceRepository
import org.di.api.impl.utils.SourceRepositoryForTesting
import org.junit.Test


class GraphCycleDetectionTest {
    @Test
    void testSelfReferencingProject() {
        SourceRepository sr = new SourceRepositoryForTesting({
            project {
                name = "quine"
                depends("quine")
            }
        })

        Graph g = new Graph(sr)
        g.initCycles()
        assert g.cycles.size() == 1
        g.cycles[0].size() == 1
        g.cycles[0][0].name == "quine"
    }

    @Test
    void testTwoProjects() {
        SourceRepository sr = new SourceRepositoryForTesting({
            project {
                name = "primus"
                depends("secundus")
            }
            project {
                name = "secundus"
                depends ("primus")
            }
        })

        Graph g = new Graph(sr)
        g.initCycles()
        assert g.cycles.size() == 1
        g.cycles[0].size() == 2
        g.cycles[0].collect {it.name}.containsAll (["primus", "secundus"])
    }
}
