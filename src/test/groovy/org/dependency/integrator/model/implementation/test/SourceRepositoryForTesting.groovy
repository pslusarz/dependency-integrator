package org.dependency.integrator.model.implementation.test

import org.dependency.integrator.model.ProjectSource
import org.dependency.integrator.model.SourceRepository

class SourceRepositoryForTesting implements SourceRepository {

    List<ProjectSource> projectSources

    SourceRepositoryForTesting(Closure config) {
        projectSources = []
        config.delegate = this
        config.call()
    }

    def project(Closure config) {
        def p = new ProjectSourceForTesting(config)
        projectSources << p
    }

    @Override
    void downloadAll(File localDir) {
        println "downloaded to: "+localDir.absolutePath
    }

    @Override
    Collection<ProjectSource> init(File localDir) {
        return projectSources
    }

    @Override
    void upload(ProjectSource project) {
        println "uploaded "+project.name+" to public source repository"
    }


}
