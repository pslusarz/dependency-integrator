package org.di.api.impl

import org.di.api.Dependency
import org.di.api.Version

class CarfaxJarDependency implements Dependency {
    private final String name;
    private final Version version;

    CarfaxJarDependency(String name, Version version) {
        this.name = name;
        this.version = version;
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
