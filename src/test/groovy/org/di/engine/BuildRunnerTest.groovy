package org.di.engine

import org.di.api.impl.utils.ProjectSourceForTesting
import org.junit.Test

class BuildRunnerTest {
    int ALLOWED_OVERHEAD_MILLIS = 10

    @Test
    void testSinglePassingProject() {
        def projects = [new ProjectSourceForTesting(
                {
                    name = "one"
                    buildShouldWork = true
                    buildTimeMillis = 15
                })]
        BuildRunner br = new BuildRunner(projectSources: projects)
        br.start()
        def results = br.completeBuildRecords
        assert results
        assert results.size() == 1
        assert results[0].projectSource.name == "one"
        assert 15 <= results[0].runTimeMillis && results[0].runTimeMillis <= 15 + ALLOWED_OVERHEAD_MILLIS
        assert results[0].result == BuildRecord.BuildResult.Passed
    }

    @Test
    void testOnePassingOneFailing() {
        def projects = [
                new ProjectSourceForTesting({
                    name = "one"
                    buildShouldWork = true
                    buildTimeMillis = 15
                }),
                new ProjectSourceForTesting({
                    name = "two"
                    buildShouldWork = false
                })
        ]
        BuildRunner br = new BuildRunner(projectSources: projects)
        br.start()
        def results = br.completeBuildRecords
        assert results.size() == 2
        assert results.find {it.projectSource.name == "one"}.result == BuildRecord.BuildResult.Passed
        assert results.find {it.projectSource.name == "two"}.result == BuildRecord.BuildResult.Failed
    }
}
