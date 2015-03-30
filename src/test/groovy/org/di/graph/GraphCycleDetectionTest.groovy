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

    @Test
    void simplestTwoCycles() {
        SourceRepository sr = new SourceRepositoryForTesting({
            project {
                name = "primus"
                depends("secundus")
                depends("tertius")
            }
            project {
                name = "secundus"
                depends ("primus")
            }
            project {
                name = "tertius"
                depends ("primus")
            }
        })

        Graph g = new Graph(sr)
        g.initCycles()
        assert g.cycles.size() == 2

        def secundusCycle = g.cycles.find {it.collect{it.name}.contains("secundus")}
        assert secundusCycle
        assert secundusCycle.size() == 2
        assert secundusCycle.collect {it.name}.contains("primus")

        def tertiusCycle = g.cycles.find {it.collect{it.name}.contains("tertius")}
        assert tertiusCycle
        assert tertiusCycle.size() == 2
        assert tertiusCycle.collect {it.name}.contains("primus")

    }
}
