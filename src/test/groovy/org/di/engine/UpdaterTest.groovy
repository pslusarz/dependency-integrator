package org.di.engine

import org.di.api.impl.utils.SourceRepositoryForTesting
import org.junit.Test

class UpdaterTest {
    @Test
    void singleDependency() {
        Updater updater = new Updater(new SourceRepositoryForTesting( {
            project {
                name = "parent"
                version = 3
                versions = [1,2,3]
            }
            project {
                name = "child"
                depends ("parent", 1)
            }
        }
        ))

        assert 2 ==  new StalenessCalculator(updater.graph).metric
        updater.update()
        assert 0 == new StalenessCalculator(updater.graph).metric
    }
}
