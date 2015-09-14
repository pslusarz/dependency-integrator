package org.di.api.impl.carfax.util

import org.di.api.impl.carfax.CarfaxGradleProjectSource

class Gradle {
    static File run(CarfaxGradleProjectSource projectSource, String command) {
        File output = new File(System.getProperty("java.io.tmpdir"), "out-" + projectSource.projectDirectory.name +"-"+System.currentTimeMillis()+ ".txt")
        File buildFile = new File(projectSource.projectDirectory, "build.gradle")
        if (buildFile.exists()) {
            Command.run("${projectSource.projectDirectory.absolutePath}\\gradlew.bat --build-file ${buildFile.absolutePath} --gradle-user-home ${projectSource.projectDirectory.absolutePath} ${command} >${output.absolutePath}")
        } else {
            output << "Failure: build.gradle not found in "+projectSource.projectDirectory
        }
        return output
    }
}
