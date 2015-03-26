package org.dependency.integrator.model.implementation.test

import org.junit.Test

class VersionForTestingTest {
    @Test
    void testBeforeAndAfter() {
        def v1 = new VersionForTesting(value: 1)
        def v2 = new VersionForTesting(value: 2)
        assert v1.before(v2)
        assert v2.after(v1)
    }

    @Test
    void testBeforeAndAfterFalseForSameVersion() {
        def v1 = new VersionForTesting(value: 1)
        def v2 = new VersionForTesting(value: 1)
        assert !v1.before(v2)
        assert !v1.after(v2)
        assert !v2.before(v1)
        assert !v2.after(v1)
        assert !v1.before(v1)
        assert !v1.after(v1)
    }

    @Test
    void testSimpleIncrement() {
        def v1 = new VersionForTesting(value: 100)
        def v2 = v1.increment()
        assert v2.after(v1)
    }

    @Test
    void testIncrementByTwo() {
        def v1 = new VersionForTesting(value: 101)
        def v2 = v1.increment()
        def v3 = v2.increment()
        assert v3.after(v1)
        assert v3.after(v2)
    }

    @Test
    void testTwoCallsToIncrementGiveSameVersion() {
        def v1 = new VersionForTesting(value: 102)
        def v2 = v1.increment()
        def v3 = v1.increment()
        assert !v3.after(v2)
        assert !v3.before(v2)
    }

}
