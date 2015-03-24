package org.dependency.integrator.model.implementation.test

import org.dependency.integrator.model.Dependency
import org.dependency.integrator.model.ProjectSource
import org.dependency.integrator.model.Version

/**
 * Created by paulslusarz on 3/24/2015.
 */
class ProjectSourceForTesting implements ProjectSource {

    String name

    ProjectSourceForTesting(Closure config) {
        config.delegate = this
        config.call()
    }

    @Override
    String getName() {
        return name
    }

    @Override
    Version getVersion() {
        return null
    }

    @Override
    void incrementVersion() {

    }

    @Override
    void publishArtifactToTestRepo() {

    }

    @Override
    Collection<Dependency> getDependencies() {
        return null
    }

    @Override
    void setDependencyVersion(Dependency dependency, Version newVersion) {

    }

    @Override
    void setDependencyGuarded(Dependency dependency) {

    }

    @Override
    boolean build() {
        return false
    }
}
