package org.di.graph

import org.di.api.SourceRepository
import org.di.api.impl.utils.SourceRepositoryForTesting
import org.junit.Test

class GraphTest {
    @Test
    void testCanHandleIndependentProject() {
        SourceRepository sr = new SourceRepositoryForTesting({
            project {
                name = "i-am-just-a-lonely-poroject"
            }
        })

        Graph g = new Graph(sr)
        assert g.nodes.size == 1
        assert g.nodes[0].name == "i-am-just-a-lonely-poroject"
    }

    @Test
    void testCanHandleOrphanedDependency() {
        SourceRepository sr = new SourceRepositoryForTesting({
            project {
                name = "i-am-just-a-lonely-poroject"
                depends ("nonexistant", 1)
            }
        })

        Graph g = new Graph(sr)
        assert g.nodes.size == 1
        assert g.nodes[0].outgoing.size() == 0
    }

    @Test
    void testOneDependency() {
        SourceRepository sr = new SourceRepositoryForTesting({
            project {
                name = "independent"
                version = 33
            }
            project {
                name = "dependent"
                depends ("independent", 33)
            }
        })

        Graph g = new Graph(sr)
        assert g.nodes.size() == 2
        assert g.nodes.find {it.name == "dependent"}.outgoing.size() == 1
        assert g.nodes.find {it.name == "dependent"}.outgoing.first().to.name == "independent"

    }





}
