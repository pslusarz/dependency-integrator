package org.di.api.impl.carfax

import groovy.util.logging.Log
import org.di.api.Dependency
import org.di.api.ProjectSource
import org.di.api.Version

@Log
class CarfaxGradleProjectSource implements ProjectSource {
    private final File projectDirectory;
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

    private initVersions() {
        if (versions.size() == 0) {
            String cmd = "${System.properties["os.name"]?.startsWith("Windows")? 'cmd /c ':''}git --git-dir=${projectDirectory.absolutePath}/.git --no-pager --work-tree=${projectDirectory.absolutePath} log --tags --grep=release --pretty=oneline"
            def proc = cmd.execute()
            proc.waitFor()
            String output = proc.text
            versions = StringMajorMinorPatchVersion.parseFromGitLog(output)
            if (versions.size() == 0) {
                log.warning "NO TAG FOR ${name}, trying version from properties"
                log.warning "  command used: "+cmd
                log.warning "  output: "+output
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

    def parseFromBuildGradle() {
        def result = []
        def lines = new File(projectDirectory, "build.gradle").readLines()
        lines.findAll { it.contains('carfax:') }.each { line -> // compile 'carfax:serialization-extensions:3.1.1'
            String depChunk = line.split(/['|"]/)[1] //carfax:serialization-extensions:3.1.1
            String[] depChunk2 = depChunk.split(':')
            def dep = new CarfaxJarDependency(depChunk2[1], new StringMajorMinorPatchVersion(depChunk2[2]), CarfaxJarDependency.DependencySource.buildfile)
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
        File output = new File(System.getProperty("java.io.tmpdir"), "out-" + projectDirectory.name + ".txt")
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
