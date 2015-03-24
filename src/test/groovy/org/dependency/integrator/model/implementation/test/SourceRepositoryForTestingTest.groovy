package org.dependency.integrator.model.implementation.test

import org.junit.Test

class SourceRepositoryForTestingTest {
    @Test
    void testAddProjectByName() {
        def srt = new SourceRepositoryForTesting({
            project {
                name = "project1"
            }

            project {
                name = "project2"
            }
        })
        def projects = srt.init(new File("blah"))
        assert projects.size() == 2
        assert projects.find {it.name == 'project1'}
        assert projects.find {it.name == 'project2'}
    }

    @Test
    void testProjectsWithDependencies() {
        def srt = new SourceRepositoryForTesting({
            project {
                name = "project1"
                version = 1
                depends("project2", 1)
            }

            project {
                name = "project2"
                version = 2
            }
        })

        def projects = srt.init(new File("blah"))

        def dependency = projects.find {it.name == "project1"}.dependencies.find {it.projectSourceName == "project2"}
        assert dependency
        assert dependency.version.before(projects.find {it.name == "project2"}.version)
    }

}
