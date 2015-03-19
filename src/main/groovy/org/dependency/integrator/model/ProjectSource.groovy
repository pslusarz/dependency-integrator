package org.dependency.integrator.model

/**
 * Created by ps on 3/18/15.
 */
interface ProjectSource {
    /**
     * This is to be assigned by the dependency analysis algorithm.
     * Projects that do not depend on other projects, have level 0
     * Level of a project depending on other projects is the highest level
     * of a dependent project, plus 1
     */
    int level = -1

    public String getName()
    /**
     * Subsequent operations on the returned version have no effect on the project version.
     * Use incrementVersion to make a permanent change.
     * @return
     */
    public Version getVersion()

    /**
     * permanently change project version in preparation to publish a new artifact
     */
    public void incrementVersion()

    /**
     * publish to a local repository where the artifact can be consumed by
     * upstream projects under this SourceRepository. Equivalent to 'maven install'
     * or 'gradle install'
     */
    public void publishArtifactToTestRepo()

    /**
     * Other projects in this SourceRepository that this project depends on
     * Ideally, every dependency should be matched to a project returned by
     * SourceRepository.init()
     * @return
     */
    public Collection<Dependency> getDependencies()

    /**
     * modify project source to use new dependency version
     * @param dependency
     * @param newVersion
     */
    public void setDependencyVersion(Dependency dependency, Version newVersion)

    /**
     * Attempt to automatically change dependency version resulted in broken build,
     * Modify project to indicate not to try to automatically increment it in the future
     * @param dependency
     */
    public void setDependencyGuarded(Dependency dependency)

    /**
     * Test the project and assemble an artifact
     * @return true if build was successful
     */
    boolean build()

}