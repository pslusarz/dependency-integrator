package org.di.api.impl.test

import org.di.api.impl.utils.SourceRepositoryForTesting
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

    @Test
    void testProjectDependencyBumpPersisted() {
        def srt = new SourceRepositoryForTesting({
            project {
                name = "project1"
                version = 1
                depends("project2", 1)
            }
        })
        def project = srt.init(new File("blah")).find {it.name == "project1"}
        def dependency = project.dependencies.find {it.projectSourceName == "project2"}
        project.setDependencyVersion(dependency, dependency.version.increment())
        assert project.dependencies.find{it.projectSourceName == "project2"}.version.after(dependency.version)
        def reinitializedProject = srt.init(new File("blah")).find {it.name == "project1"}
        assert reinitializedProject.dependencies.find{it.projectSourceName == "project2"}.version.after(dependency.version)


    }

}
