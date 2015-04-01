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

    @Test
    void simplestTwoCyclesDoNotLookDifferentDependingOnWhereYouStartSearching() {
        SourceRepository sr = new SourceRepositoryForTesting({
            project {
                name = "secundus"
                depends("primus")
            }
            project {
                name = "tertius"
                depends("primus")
            }
            project {
                name = "primus"
                depends("secundus")
                depends("tertius")
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

    @Test
    void disconnectedCycles() {
        SourceRepository sr = new SourceRepositoryForTesting({
            project {
                name = "secundus"
                depends("primus")
            }
            project {
                name = "primus"
                depends("secundus")
            }
            project {
                name = "tertius"
                depends("quartus")
            }
            project {
                name = "quartus"
                depends("tertius")
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
        assert tertiusCycle.collect {it.name}.contains("quartus")
    }

    @Test
    void mixOfMultinodeCyclesAndTrees() {
        SourceRepository sr = new SourceRepositoryForTesting({
            project {
                name = "lonely"
            }
            project {
                name = "cycle4-1"
                depends("cycle4-2")
            }
            project {
                name = "cycle4-2"
                depends("cycle4-3")
            }
            project {
                name = "cycle4-3"
                depends("cycle4-4")
            }
            project {
                name = "cycle4-4"
                depends("cycle4-1")
            }

            project {
                name = "tree1"
                depends("tree2")
            }

            project {
                name = "tree2-1"
                depends("tree2-2")
            }

            project {
                name = "tree2-2"
            }

            project {
                name = "tree2cycle-1"
                depends("tree2cycle-2")
            }

            project {
                name = "tree2cycle-2"
                depends("cycle2-1")
            }

            project {
                name = "cycle2-1"
                depends("cycle2-2")
            }

            project {
                name = "cycle2-2"
                depends("cycle2-1")
            }
        })

        Graph g = new Graph(sr)
        g.initCycles()
        assert g.cycles.size() == 2

        def cycle4 = g.cycles.find {it.collect{it.name}.contains("cycle4-1")}
        assert cycle4
        assert cycle4.size() == 4
        assert cycle4.collect {it.name}.containsAll(["cycle4-1","cycle4-2", "cycle4-3", "cycle4-4"])

        def cycle2 = g.cycles.find {it.collect{it.name}.contains("cycle2-1")}
        assert cycle2
        assert cycle2.size() == 2
        assert cycle2.collect {it.name}.containsAll(["cycle2-1","cycle2-2"])
    }
}
