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

    @Test
    void twoLevelDependency() {
        Updater updater = new Updater(new SourceRepositoryForTesting( {
            project {
                name = "parent"
                version = 3
                versions = [1,2,3]
            }
            project {
                name = "child"
                version = 1
                versions = [1]
                depends ("parent", 1)
            }
            project {
               name = "grandchild"
                depends ("child", 1)
            }
        }
        ))

        assert 4 ==  new StalenessCalculator(updater.graph).metric
        updater.updateRank(2)
        assert 1 == new StalenessCalculator(updater.graph).metric //new version of "child"
        updater.updateRank(3)
        assert 0 == new StalenessCalculator(updater.graph).metric
    }
}
