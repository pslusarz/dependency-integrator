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
}
