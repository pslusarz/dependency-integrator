package org.di.api.impl.utils

import groovy.transform.ToString
import org.di.api.Dependency
import org.di.api.Version
import org.di.api.ProjectSource

@ToString(includePackage = false)
class ProjectSourceForTesting implements ProjectSource {

    String name
    int version = 1
    List<DependencyForTesting> dependencies = []

    ProjectSourceForTesting(Closure config) {
        config.delegate = this
        config.call()
    }

    def depends(String name, int version = 1) {
        def dep = new DependencyForTesting(projectSourceName: name, version: new VersionForTesting(value: version))
        dependencies << dep
    }

    @Override
    String getName() {
        return name
    }

    @Override
    Version getVersion() {
        return new VersionForTesting(value: version)
    }

    @Override
    void incrementVersion() {
      version ++
    }

    @Override
    void publishArtifactToTestRepo() {

    }

    @Override
    Collection<Dependency> getDependencies() {
        def result =  []
        result.addAll(dependencies)
        return result
    }

    @Override
    void setDependencyVersion(Dependency dependency, Version newVersion) {
      dependencies.remove (dependencies.find {it.projectSourceName == dependency.projectSourceName})
      dependencies << new DependencyForTesting(projectSourceName: dependency.projectSourceName, version: newVersion)
    }

    @Override
    void setDependencyGuarded(Dependency dependency) {

    }

    @Override
    boolean build() {
        return false
    }
}
