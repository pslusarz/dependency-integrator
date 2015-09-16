package org.di.api.impl.carfax.util

import org.di.api.impl.carfax.CarfaxGradleProjectSource

class Gradle {
    static File run(CarfaxGradleProjectSource projectSource, String command) {
        File output = new File(System.getProperty("java.io.tmpdir"), "out-" + projectSource.projectDirectory.name +"-"+System.currentTimeMillis()+ ".txt")
        File buildFile = new File(projectSource.projectDirectory, "build.gradle")
        if (buildFile.exists()) {
            output << Command.run("${projectSource.projectDirectory.absolutePath}${File.separator}gradlew${Command.isWindows()?'.bat':''} --build-file ${buildFile.absolutePath} --gradle-user-home ${projectSource.projectDirectory.absolutePath} ${command}")
        } else {
            output << "Failure: build.gradle not found in "+projectSource.projectDirectory
        }
        if (!output.exists()) {
            output << "Failure: gradle build did not produce output to stdout, even though the build file exists."
        }
        return output
    }
}
