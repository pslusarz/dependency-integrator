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

}
