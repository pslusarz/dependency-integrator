package org.di.api.impl.carfax

import groovy.util.logging.Log
import org.di.api.Dependency
import org.di.api.ProjectSource
import org.di.api.Version

@Log
class ImmutableProjectSource {
    File projectDirectory
    Version version
    List<Dependency> dependencies

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

    Collection<Dependency> getDependencies() {
        if (dependencies) {
            return dependencies
        } else {
            dependencies = parseFromDependenciesProperties()
            dependencies.addAll(parseFromBuildGradle())
            return dependencies
        }
    }

    def parseFromBuildGradle() {
        def result = []
        def lines = new File(projectDirectory, "build.gradle").readLines()
        lines.findAll { it.contains('carfax:') }.each { line -> // compile 'carfax:serialization-extensions:3.1.1'
            try {
                String depChunk = line.split(/['|"]/)[1] //carfax:serialization-extensions:3.1.1
                String[] depChunk2 = depChunk.split(':')
                def dep = new CarfaxJarDependency(depChunk2[1], new StringMajorMinorPatchVersion(depChunk2[2]), CarfaxJarDependency.DependencySource.buildfile)
                result << dep
            } catch (NumberFormatException e) {
                log.warning "IGNORING DEPENDENCY: could not parse version number in this line: "+line
            }

        }

        return result

    }

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

}
