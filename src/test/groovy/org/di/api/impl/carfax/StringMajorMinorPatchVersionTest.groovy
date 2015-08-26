package org.di.api.impl.carfax

import org.di.api.Version
import org.junit.Test

class StringMajorMinorPatchVersionTest {
    @Test
    void testComparisonMajorVersions() {
        StringMajorMinorPatchVersion smaller = new StringMajorMinorPatchVersion("1.0.0")
        StringMajorMinorPatchVersion bigger = new StringMajorMinorPatchVersion("2.0.0")
        assert smaller.before(bigger)
        assert bigger.after(smaller)
    }

    @Test
    void testComparisonMinorVersions() {
        StringMajorMinorPatchVersion smaller = new StringMajorMinorPatchVersion("2.0.0")
        StringMajorMinorPatchVersion bigger = new StringMajorMinorPatchVersion("2.1.0")
        assert smaller.before(bigger)
        assert bigger.after(smaller)
    }

    @Test
    void testComparisonPatchVersions() {
        StringMajorMinorPatchVersion smaller = new StringMajorMinorPatchVersion("22.0.44")
        StringMajorMinorPatchVersion bigger = new StringMajorMinorPatchVersion("22.0.45")
        assert smaller.before(bigger)
        assert bigger.after(smaller)
    }

    @Test
    void testEquals() {
        StringMajorMinorPatchVersion one = new StringMajorMinorPatchVersion("10.34.99")
        StringMajorMinorPatchVersion two = new StringMajorMinorPatchVersion("10.34.99")
        assert !one.before(two)
        assert !two.after(one)
        assert one == two
    }

    @Test
    void doYourBestWithSnapshots() {
        StringMajorMinorPatchVersion snapshot = new StringMajorMinorPatchVersion("2.3.4-SNAPSHOT")
        assert snapshot.toString() == "2.3.3"

        StringMajorMinorPatchVersion snapshot2 = new StringMajorMinorPatchVersion("2.3.0-SNAPSHOT")
        assert snapshot2.toString() == "2.2.0"
    }

    @Test
    void handleUnderscores() {
        StringMajorMinorPatchVersion version = new StringMajorMinorPatchVersion("1.9_2")
        assert version.major == 1
        assert version.minor == 9
        assert version.patch == 2
    }

    @Test
    void handleTwoComponentVersions() {
        StringMajorMinorPatchVersion version = new StringMajorMinorPatchVersion("1.2004")
        assert version.major == 1
        assert version.minor == 2004
        assert version.patch == 0
    }


}
