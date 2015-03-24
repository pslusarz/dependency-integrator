package org.dependency.integrator.model.implementation.test

import org.dependency.integrator.model.Version

class VersionForTesting implements Version {

    int value

    @Override
    Version increment() {
        return new VersionForTesting(value: value+1)
    }

    @Override
    boolean before(Version other) {
        assert other instanceof VersionForTesting
        return value < (other as VersionForTesting).value
    }

    @Override
    boolean after(Version other) {
        return !before(other)
    }
}
