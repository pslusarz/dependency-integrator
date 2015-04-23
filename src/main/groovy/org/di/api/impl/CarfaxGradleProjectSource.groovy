package org.di.api.impl

import org.di.api.Dependency
import org.di.api.ProjectSource
import org.di.api.Version


class CarfaxGradleProjectSource implements ProjectSource {
    private final File projectDirectory;

    public CarfaxGradleProjectSource(File projectDirectory) {
        this.projectDirectory = projectDirectory
    }

    @Override
    String getName() {
        return projectDirectory.name
    }

    boolean hasBuildFile() {
        new File(projectDirectory, "build.gradle").exists()
    }

    boolean hasCarfaxDependencyInBuildFile() {
        if (hasBuildFile()) {
            String text = new File(projectDirectory, "build.gradle").text
            return text.contains('carfax:') || text.contains("""group: 'carfax'""") || text.contains("""group: "carfax\"""")
        }
        return false
    }

    boolean hasManagedDependenciesFile() {
        return new File(projectDirectory, "dependencies.properties").exists()
    }

    @Override
    Version getVersion() {
        def props = new Properties()
        props.load(new File(projectDirectory, "gradle.properties").newReader())
        assert props.version
        return new StringMajorMinorPatchVersion(props.version)
    }

    @Override
    void incrementVersion() {

    }

    @Override
    void publishArtifactToTestRepo() {

    }

    @Override
    Collection<Dependency> getDependencies() {
        if (dependencies) {
            return dependencies
        } else {
            dependencies = parseFromDependenciesProperties()
            dependencies.addAll(parseFromBuildGradle())
            return dependencies
        }
    }

    @Override
    void setDependencyVersion(Dependency dependency, Version newVersion) {

    }

    def parseFromBuildGradle() {
        def result = []
        def lines = new File(projectDirectory, "build.gradle").readLines()
        lines.findAll { it.contains('carfax:') }.each { line -> // compile 'carfax:serialization-extensions:3.1.1'
            String depChunk = line.split(/['|"]/)[1] //carfax:serialization-extensions:3.1.1
            String[] depChunk2 = depChunk.split(':')
            result << new CarfaxJarDependency(depChunk2[1], new org.di.api.impl.StringMajorMinorPatchVersion(depChunk2[2]))

        }

        return result

    }

    def dependencies

    def parseFromDependenciesProperties() {
        try {
            File dependencyPropertyFile = new File(projectDirectory, "dependencies.properties")
            List<Dependency> dependencies = [];
            dependencyPropertyFile.eachLine { String line ->
                if (line) {
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        dependencies.add(new CarfaxJarDependency(parts[0], new StringMajorMinorPatchVersion(parts[1])));
                    }
                }
            }
            return dependencies;
        } catch (IOException e) {
            return []
        }
    }


    @Override
    void setDependencyGuarded(Dependency dependency) {

    }

    @Override
    boolean build() {
        String cmd = "cmd /c ${projectDirectory.absolutePath}\\gradlew.bat build"
        def proc = cmd.execute()
        //proc.consumeProcessOutput()
        //synchronized (this) {println proc.text}
        proc.waitFor()
        return true
    }
}
