package org.di.graph.visualization

import org.di.api.SourceRepository
import org.di.api.impl.utils.SourceRepositoryForTesting
import org.di.graph.Graph
import org.junit.Test

class GraphVizGeneratorTest {
    @Test
    void happyPath() {
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
        GraphVizGenerator gvg = new GraphVizGenerator(graph: g)
        assert !gvg.script.exists()
        assert !gvg.graphic.exists()
        gvg.generate()
        assert gvg.script.exists()
        assert gvg.script.text.contains("digraph")
        assert gvg.script.text.contains("primus")
        assert gvg.script.text.contains("secundus")
        assert gvg.script.text.contains("tertius")
        assert gvg.graphic.exists()
        //gvg.reveal()

    }
}
