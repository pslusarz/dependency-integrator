package org.di.api.impl

import org.di.api.ProjectSource
import org.di.api.SourceRepository


class CarfaxLibSourceRepository implements SourceRepository {
    File localDir


    @Override
    Collection<ProjectSource> init() {
        def result = []
        localDir.eachDir { File projectDir ->
            result.add(new CarfaxGradleProjectSource(projectDir))
        }
        return result
    }

    @Override
    File getLocalDir() {
        return localDir
    }

    @Override
    void setLocalDir(File localDir) {
        this.localDir = localDir
    }

    @Override
    void downloadAll() {

    }


    @Override
    void upload(ProjectSource project) {

    }
}
