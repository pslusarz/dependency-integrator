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
    void testFailWhenExceptionInBuild() {
        def projects = [new ProjectSourceForTesting() {
            @Override
            boolean build() {
                throw new RuntimeException("thou shall never build me")
            }
        }]
        BuildRunner br = new BuildRunner(projectSources: projects)
        br.start()
        def results = br.completeBuildRecords
        assert results
        assert results.size() == 1
        assert results[0].result == BuildRecord.BuildResult.Failed
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

    @Test
    void testBuildsAreRunInParallel() {
        def runtimes = [103, 45, 67, 115, 98, 325, 75, 245, 90, 12, 172, 55]
        def projects = runtimes.collect { int runtime ->
            new ProjectSourceForTesting({
                name = "project-"+runtime.toString()
                buildTimeMillis = runtime
            })
        }
        BuildRunner br = new BuildRunner(projectSources: projects)
        def start = System.currentTimeMillis()
        br.start()
        def results = br.completeBuildRecords
        def stop = System.currentTimeMillis()

        assert results.sum {it.runTimeMillis} >= runtimes.sum()
        assert (stop-start) < runtimes.sum()
    }
}
