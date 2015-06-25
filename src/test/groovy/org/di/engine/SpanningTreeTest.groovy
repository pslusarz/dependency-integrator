package org.di.engine

import org.di.api.SourceRepository
import org.di.api.impl.utils.SourceRepositoryForTesting
import org.di.graph.Graph
import org.junit.Test


class SpanningTreeTest {

    @Test
    void singleNode() {
        SourceRepository sr = new SourceRepositoryForTesting({
            project {
                name = "root"
            }
        })
        Graph g = new Graph(sr)
        def st = new SpanningTreeBuilder(world: g, treeRoot: "root")
        def results = st.connectedProjects
        assert results.size() == 1
        assert results.find {it.name == 'root'}
    }

    @Test
    void projectNotInGraph() {
        SourceRepository sr = new SourceRepositoryForTesting({
            project {
                name = "root"
            }
            project {
                name = "lev1-1"
                depends("root")
            }
            project {
                name = "lev1-2"
                depends("root")
            }
            project {
                name = "lev2-1"
                depends("lev1-1")
                depends("bad guy")
            }
        })
        Graph g = new Graph(sr)
        def st = new SpanningTreeBuilder(world: g, treeRoot: "notHere")
        def results = st.connectedProjects
        assert results.size() == 0
    }

    @Test
    void happyPath() {
        SourceRepository sr = new SourceRepositoryForTesting({
            project {
                name = "root"
            }
            project {
                name = "lev1-1"
                depends ("root")
            }
            project {
                name = "lev1-2"
                depends ("root")
            }
            project {
                name = "lev2-1"
                depends ("lev1-1")
                depends ("bad guy")
            }
            project {
                name = "lev2-2"
                depends ("root")
                depends ("lev1-1")
            }
            project {
                name = "bad guy"
            }
            project {
                name = "disconnect"
            }
        })

        Graph g = new Graph(sr)
        def st = new SpanningTreeBuilder(world: g, treeRoot: "root")
        def results = st.connectedProjects
        assert results.size() == 5
        assert results.find {it.name == 'root'}
        assert results.find {it.name == 'lev1-1'}
        assert results.find {it.name == 'lev1-2'}
        assert results.find {it.name == 'lev2-1'}
        assert results.find {it.name == 'lev2-2'}

    }
}
