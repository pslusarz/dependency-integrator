package org.di.engine

import org.di.api.Dependency
import org.di.api.ProjectSource
import org.di.api.Version

class BulkDependencyIncrementer {
    ProjectSource projectSource
    Collection<ProjectSource> projectSources
    Map<Dependency, Version> originalVersions = [:]

    void increment() {
        projectSource.dependencies.each { Dependency dependency ->
            def isPhantomDependency = !projectSources.find { it.name == dependency.projectSourceName }
            if (!isPhantomDependency && dependency.version.before(projectSources.find { it.name == dependency.projectSourceName }.version)) {
                originalVersions[dependency] = dependency.version
                projectSource.setDependencyVersion(dependency, projectSources.find {
                    it.name == dependency.projectSourceName
                }.version)
            }
        }
    }

    void rollback() {
        originalVersions.each { Dependency dependency, Version originalVersion ->
            projectSource.setDependencyVersion(dependency, originalVersion)
            projectSource.setDependencyGuarded(dependency)
        }
    }

}
