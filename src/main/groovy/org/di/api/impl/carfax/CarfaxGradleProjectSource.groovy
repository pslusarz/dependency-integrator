package org.di.api.impl.carfax

import groovy.util.logging.Log
import org.di.api.Dependency
import org.di.api.ProjectSource
import org.di.api.Version
import org.di.api.impl.carfax.util.Command
import org.di.api.impl.carfax.util.Git
import org.di.api.impl.carfax.util.GitVersionTag
import org.di.api.impl.carfax.util.Gradle

@Log
class CarfaxGradleProjectSource extends ImmutableProjectSource implements ProjectSource {
    private List<Version> versions = []


    public CarfaxGradleProjectSource(File projectDirectory) {
        this.projectDirectory = projectDirectory
    }

    @Override
    String toString() {
        projectDirectory.name
    }

    @Override
    String getName() {
        return projectDirectory.name
    }


    private initVersions() {
        if (versions.size() == 0) {

            String output = Git.getVersionTags(projectDirectory)
            versions = GitVersionTag.parseFromGitLog(output).collect { it.version }
            if (versions.size() == 0) {
                log.warning "NO TAG FOR ${name}, trying version from properties"
                log.warning "  git output: " + output
                versions = [getVersionFromProperties()]
            }
        }
    }

    @Override
    public List<Version> getVersions() {
        initVersions()
        def result = []
        result.addAll(versions)
        return result
    }

    @Override
    Version getLatestVersion() {
        initVersions()
        return versions.last()
    }

    Version getVersionFromProperties() {
        def props = new Properties()
        props.load(new File(projectDirectory, "gradle.properties").newReader())
        assert props.version
        return new StringMajorMinorPatchVersion(props.version)
    }

    @Override
    void incrementVersion() {
        /**
         * 1. run gradle bump version
         * 2. get new version from properties
         * 3. add it to the versions list... or tag and re-init?
         */
        throw new RuntimeException("not implemented")
    }

    @Override
    void publishArtifactToTestRepo() {
        Gradle.run(this, "install")
    }


    @Override
    void setDependencyVersion(Dependency dependency, Version newVersion) {
        if (dependency.source == CarfaxJarDependency.DependencySource.properties) {
            Properties props = new Properties()
            File depsFile = new File(projectDirectory, "dependencies.properties")

            props.load(depsFile.newInputStream())
            props.store(new File(projectDirectory, "old-dependencies-${System.currentTimeMillis()}.properties").newOutputStream(), "backup before dependency update")
            props[dependency.projectSourceName] = newVersion.toString()
            props.store(new File(projectDirectory, "dependencies.properties").newOutputStream(), "updated with dependency updater")
            dependencies = null    //todo: TEST me!
        } else {
            log.warning "I don't know yet how to update build.gradle dependencies in ${name}: " + dependency.projectSourceName
        }
    }


    @Override
    void setDependencyGuarded(Dependency dependency) {
        //I'm beginning to think this is unnecessary
    }

    @Override
    boolean build() {
        return Gradle.run(this, "clean build").text.contains("BUILD SUCCESSFUL")
    }
}
