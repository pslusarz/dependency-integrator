package org.dependency.integrator.model.implementation.test

import groovy.transform.ToString
import org.dependency.integrator.model.Dependency
import org.dependency.integrator.model.Version

@ToString
class DependencyForTesting implements Dependency{
    Version version
    String projectSourceName


    @Override
    Version getVersion() {
        return version
    }

    @Override
    String getProjectSourceName() {
        return projectSourceName
    }

    @Override
    boolean isFixed() {
        return false
    }

    @Override
    boolean isGuarded() {
        return false
    }
}
