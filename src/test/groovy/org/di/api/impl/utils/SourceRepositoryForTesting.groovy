package org.di.api.impl.utils

import org.di.api.ProjectSource
import org.di.api.SourceRepository

class SourceRepositoryForTesting implements SourceRepository {

    List<ProjectSource> projectSources
    File localDir

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
    void downloadAll() {
        println "downloaded to: "+localDir?.absolutePath
    }

    @Override
    Collection<ProjectSource> init() {
        return projectSources
    }

    @Override
    void upload(ProjectSource project) {
        println "uploaded "+project.name+" to public source repository"
    }

    @Override
    void setLocalDir(File localDir) {
        this.localDir = localDir
    }

}
