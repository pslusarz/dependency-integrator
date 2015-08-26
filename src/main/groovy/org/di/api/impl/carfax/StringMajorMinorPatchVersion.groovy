package org.di.api.impl.carfax

import groovy.util.logging.Log;
import org.di.api.Version;

@Log
public class StringMajorMinorPatchVersion implements Version {
    private final String value;
    long major = -1
    long minor = -1
    long patch = -1

    public StringMajorMinorPatchVersion(String v) {
        String k = v
        if (k.contains("-SNAPSHOT")) {
            k = k - '-SNAPSHOT'
            long[] versions = mmpFromString(k)
            if (versions[2] == 0) {
                versions[1] = versions[1] - 1
            } else (
                    versions[2] = versions[2] - 1
            )
            k = versions.collect { it.toString() }.join(".")
            assignMMP(versions)
        } else {
            assignMMP(mmpFromString(v))
        }
        this.value = k;
    }

    private assignMMP(long[] versions) {
        major = versions[0]
        minor = versions[1]
        if (versions.size() == 2) {
            patch = 0
        } else {
            patch = versions[2]
        }
    }

    private static long[] mmpFromString(String input) {  // "2.3.4" or "1.9_2"
        input.split(/[\.[_]]/).collect { Long.parseLong(it) }.toArray()

    }

    public String getValue() {
        return value;
    }

    @Override
    public Version increment() {
        return null;
    }

    @Override
    public boolean before(Version other) {
        assert other instanceof StringMajorMinorPatchVersion
        return other.asLong() > this.asLong()
    }

    @Override
    public boolean after(Version other) {
        return !before(other) && !equals(other);
    }

    @Override
    boolean equals(Object o) {
        boolean result = false
        if (o instanceof StringMajorMinorPatchVersion) {
            result = value == o.value
        }
        result
    }

    private long asLong() {
        1000000 * major + 10000 * minor + patch
    }

    @Override
    public int hashCode() {
        value.hashCode()
    }

    @Override
    String toString() {
        return value
    }

}
