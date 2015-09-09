package org.di.api.impl.carfax

import groovy.util.logging.Log
import org.di.api.Dependency
import org.di.api.ProjectSource
import org.di.api.Version
import org.di.api.impl.carfax.util.Command
import org.di.api.impl.carfax.util.Git
import org.di.api.impl.carfax.util.GitVersionTag

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
            versions = GitVersionTag.parseFromGitLog(output).collect {it.version}
            if (versions.size() == 0) {
                log.warning "NO TAG FOR ${name}, trying version from properties"
                log.warning "  git output: "+output
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
       throw new RuntimeException("not implemented")
    }

    @Override
    void publishArtifactToTestRepo() {

    }


    @Override
    void setDependencyVersion(Dependency dependency, Version newVersion) {
        if (dependency.source == CarfaxJarDependency.DependencySource.properties) {
            Properties props = new Properties()
            File depsFile = new File(projectDirectory, "dependencies.properties")

            props.load(depsFile.newInputStream())
            props.store(new File(projectDirectory, "old-dependencies-${System.currentTimeMillis()}.properties").newOutputStream(),"backup before dependency update")
            props[dependency.projectSourceName] = newVersion.toString()
            props.store(new File(projectDirectory, "dependencies.properties").newOutputStream(), "updated with dependency updater")
            dependencies = null    //todo: TEST me!
        } else {
            log.warning "I don't know yet how to update build.gradle dependencies in ${name}: "+dependency.projectSourceName
        }
    }


    @Override
    void setDependencyGuarded(Dependency dependency) {

    }

    @Override
    boolean build() {
        File output = new File(System.getProperty("java.io.tmpdir"), "out-" + projectDirectory.name + ".txt")
        File buildFile = new File(projectDirectory, "build.gradle")
        if (buildFile.exists()) {
            Command.run("${projectDirectory.absolutePath}\\gradlew.bat --build-file ${buildFile.absolutePath} --gradle-user-home ${projectDirectory.absolutePath} clean build >${output.absolutePath}")
            return output.text.contains("BUILD SUCCESSFUL")
        } else {
            return false
        }
    }
}
