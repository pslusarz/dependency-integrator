package org.di.api.impl;

import org.di.api.Version;

public class StringMajorMinorPatchVersion implements Version {
    private final String value;
    int major = -1
    int minor = -1
    int patch = -1

    public StringMajorMinorPatchVersion(String v) {
        String k = v
        if (k.contains("-SNAPSHOT")) {
          k = k-'-SNAPSHOT'
          String[] chunks = k.split(/\./)
          int[] versions = chunks.collect {Integer.parseInt(it)} .toArray()
            if (versions[2]==0) {
                versions[1] = versions[1]-1
            } else (
              versions[2] = versions[2]-1
            )
            k = versions.collect {it.toString()}.join(".")
            major = versions[0]
            minor = versions[1]
            patch = versions[2]

        }
        this.value = k;
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
        return false
    }

    @Override
    public boolean after(Version other) {
        return false;
    }

    @Override
    String toString() {
        return value
    }
}
