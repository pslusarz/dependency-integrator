package org.dependency.integrator.model

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString
interface Version {
    public String value

    /**
     * Produce a copy, such that
     * a.increment().after(a)
     * and
     * a.before(a.increment())
     *
     * Use ProjectSource.incrementVersion() or ProjectSource.setDependencyVersion()
     * to make a permanent change
     */
    Version increment()

    /**
     *
     * @param other
     * @return has this artifact been produced before the other one
     */
    boolean before(Version other)

    /**
     *
     * @param other
     * @return has this artifact been produced after the other one
     */
    boolean after(Version other)
}