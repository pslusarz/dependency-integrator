package org.di.engine

import org.di.api.ProjectSource
import org.di.api.impl.utils.ProjectSourceForTesting
import org.di.api.impl.utils.VersionForTesting
import org.junit.Test

class BulkDependencyIncrementerTest {
    @Test
    void testSingleDep() {
        def projects = [
                new ProjectSourceForTesting({
                    name = "one"
                    depends ("two", 1)

                }),
                new ProjectSourceForTesting({
                    name = "two"
                    version = 2
                })
        ]
        new BulkDependencyIncrementer(projectSource: projects.find {it.name == "one"}, projectSources: projects).increment()
        assert projects.find {it.name == "one"}.dependencies.find{it.projectSourceName == "two"}.version == new VersionForTesting(value: 2)
    }

    @Test
    void handleNonExistantDependency() {
        def projects = [
                new ProjectSourceForTesting({
                    name = "one"
                    depends ("two", 1)

                })
        ]
        new BulkDependencyIncrementer(projectSource: projects.find {it.name == "one"}, projectSources: projects).increment()
        assert projects.find {it.name == "one"}.dependencies.find{it.projectSourceName == "two"}.version == new VersionForTesting(value: 1)
    }

    @Test
    void testHandleCurrentDependency() {
        def projects = [
                new ProjectSourceForTesting({
                    name = "one"
                    depends ("current", 2)
                    depends ("stale", 2)

                }),
                new ProjectSourceForTesting({
                    name = "current"
                    version = 2
                }),
                new ProjectSourceForTesting({
                    name = "stale"
                    version = 3
                })
        ]
        def di = new BulkDependencyIncrementer(projectSource: projects.find {it.name == "one"}, projectSources: projects)
        di.increment()
        def currentDependency = projects.find {it.name == "one"}.dependencies.find{it.projectSourceName == "current"}
        def staleDependency = projects.find {it.name == "one"}.dependencies.find{it.projectSourceName == "stale"}
        assert currentDependency.version == new VersionForTesting(value: 2)
        assert staleDependency.version == new VersionForTesting(value: 3)

    }

    @Test
    void testRollback() {
        def projects = [
                new ProjectSourceForTesting({
                    name = "one"
                    depends ("two", 1)
                    depends ("three", 3)

                }),
                new ProjectSourceForTesting({
                    name = "two"
                    version = 2
                }),
                new ProjectSourceForTesting({
                    name = "three"
                    version = 3
                })
        ]
        def di = new BulkDependencyIncrementer(projectSource: projects.find {it.name == "one"}, projectSources: projects)
        di.increment()
        di.rollback()
        def rolledbackDependency = projects.find {it.name == "one"}.dependencies.find{it.projectSourceName == "two"}
        def untouchedDependency = projects.find {it.name == "one"}.dependencies.find{it.projectSourceName == "three"}
        assert rolledbackDependency.version == new VersionForTesting(value: 1)
        assert rolledbackDependency.guarded
        assert untouchedDependency.version == new VersionForTesting(value: 3)
        assert !untouchedDependency.guarded

    }
}
