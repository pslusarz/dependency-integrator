package org.dependency.integrator.model

/**
 * Dependency that a project has on a versioned artifact produced by another project
 */
interface Dependency {

    /**
     *
     * @return a copy representing the artifact version that the project depends on
     */
    Version getVersion()

    /**
     * Should be able to find corresponding ProjectSource by its name property
     */
    String getProjectSourceName()

    /**
     * If true, then this dependency should not be automatically changed in the project
     * @return
     */
    boolean isFixed()

    /**
     * Attempt at automatically incrementing this dependency has resulted in a failed build
     * @return
     */
    boolean isGuarded()
}