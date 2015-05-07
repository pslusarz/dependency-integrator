package org.di.engine

import org.di.api.Dependency
import org.di.api.ProjectSource

class BulkDependencyIncrementer {
    ProjectSource projectSource
    Collection<ProjectSource> projectSources
    void increment() {
        projectSource.dependencies.each { Dependency dependency ->
            if (dependency.version.before(projectSources.find {it.name == dependency.projectSourceName}.version)) {
                projectSource.setDependencyVersion(dependency, projectSources.find {it.name == dependency.projectSourceName}.version)
            }
        }
    }

}
