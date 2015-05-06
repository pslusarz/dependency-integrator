package org.di.api.impl

import groovy.util.logging.Log
import org.di.api.Dependency
import org.di.api.ProjectSource
import org.di.api.Version

@Log
class CarfaxGradleProjectSource implements ProjectSource {
    private final File projectDirectory;
    private Version version

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
        if (!version) {
            String cmd = "cmd /c git --git-dir=${projectDirectory.absolutePath}\\.git --no-pager --work-tree=${projectDirectory.absolutePath} log master -5 --tags --grep=release --pretty=oneline"
            def proc = cmd.execute()
            proc.waitFor()
            List<String> versions = (proc.text).trim().split("\n")
            List<String> gitLogLineChunks = versions.first().split(" ")
            if (gitLogLineChunks.size() > 2) {
                version = new StringMajorMinorPatchVersion(gitLogLineChunks[-3] - "-SNAPSHOT")
            } else {
                log.warning "NO TAG FOR ${name}, trying version from properties"
                version = getVersionFromProperties()
            }
        }
        return version
    }

    Version getVersionFromProperties() {
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
            def dep = new CarfaxJarDependency(depChunk2[1], new org.di.api.impl.StringMajorMinorPatchVersion(depChunk2[2]), CarfaxJarDependency.DependencySource.buildfile)
            result << dep

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
                        def dep = new CarfaxJarDependency(parts[0], new StringMajorMinorPatchVersion(parts[1]), CarfaxJarDependency.DependencySource.properties)
                        dependencies.add(dep);
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
        File output = new File(System.getProperty("java.io.tmpdir"), "out-"+projectDirectory.name+".txt")
        File buildFile = new File(projectDirectory, "build.gradle")
        if (buildFile.exists()) {
            String cmd = "cmd /c ${projectDirectory.absolutePath}\\gradlew.bat --build-file ${buildFile.absolutePath} --gradle-user-home ${projectDirectory.absolutePath} clean build >${output.absolutePath}"
            //--gradle-user-home ${projectDirectory.absolutePath}\\.gradle
            def proc = cmd.execute()
            //proc.consumeProcessOutput()
            //synchronized (this) {println proc.text}
            proc.waitFor()

            return output.text.contains("BUILD SUCCESSFUL")
        } else {
            return false
        }
    }
}
