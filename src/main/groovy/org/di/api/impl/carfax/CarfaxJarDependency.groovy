package org.di.api.impl.carfax

import org.di.api.Dependency
import org.di.api.Version

class CarfaxJarDependency implements Dependency {
    private final String name;
    private final Version version;
    enum DependencySource {buildfile, properties}
    DependencySource source

    CarfaxJarDependency(String name, Version version, DependencySource source) {
        this.name = name;
        this.version = version;
        this.source = source
    }

    @Override
    Version getVersion() {
        return version;
    }

    @Override
    String getProjectSourceName() {
        return name;
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
