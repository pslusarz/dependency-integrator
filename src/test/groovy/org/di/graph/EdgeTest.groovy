package org.di.graph

import org.di.api.SourceRepository
import org.di.api.impl.utils.SourceRepositoryForTesting
import org.di.api.impl.utils.VersionForTesting
import org.junit.Test

class EdgeTest {
    @Test
    void testStale() {
        SourceRepository sr = new SourceRepositoryForTesting({
            project {
                name = "i-am-just-a-lonely-dude"
                depends ("you-depend-on-me", 1)
            }
            project {
                name = "you-depend-on-me"
                version = 2
            }
        })
        Graph g = new Graph(sr)
        Node dude = g.nodes.find{it.projectSource.name == "i-am-just-a-lonely-dude"}
        def edge = dude.outgoing.first()
        assert edge.isStale()

    }

    @Test
    void testNotStale() {
        SourceRepository sr = new SourceRepositoryForTesting({
            project {
                name = "i-am-just-a-lonely-dude"
                depends ("you-depend-on-me", 1)
            }
            project {
                name = "you-depend-on-me"
                version = 1
            }
        })
        Graph g = new Graph(sr)
        Node dude = g.nodes.find{it.projectSource.name == "i-am-just-a-lonely-dude"}
        def edge = dude.outgoing.first()
        assert !edge.isStale()

    }

    @Test
    void testSetDependency() {
        SourceRepository sr = new SourceRepositoryForTesting({
            project {
                name = "i-am-just-a-lonely-dude"
                depends ("you-depend-on-me", 1)
            }
            project {
                name = "you-depend-on-me"
                version = 2
            }
        })
        Graph g = new Graph(sr)
        Node dude = g.nodes.find{it.projectSource.name == "i-am-just-a-lonely-dude"}
        def edge = dude.outgoing.first()
        assert edge.dependency.version == new VersionForTesting(value: 1) //precondition
        assert edge.from.projectSource.dependencies.find {it.projectSourceName == "you-depend-on-me"}.version == new VersionForTesting(value: 1) //precondition

        edge.setDependencyVersion(new VersionForTesting(value: 2))
        assert edge.dependency.version == new VersionForTesting(value: 2)
        assert edge.from.projectSource.dependencies.find {it.projectSourceName == "you-depend-on-me"}.version == new VersionForTesting(value: 2)

    }
}
