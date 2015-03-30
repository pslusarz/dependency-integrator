package org.di.api.impl.utils

import groovy.transform.ToString
import org.di.api.Dependency
import org.di.api.Version

@ToString(includePackage = false)
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
