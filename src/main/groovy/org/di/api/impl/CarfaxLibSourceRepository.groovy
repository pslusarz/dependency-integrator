package org.di.api.impl

import org.di.api.ProjectSource
import org.di.api.SourceRepository


class CarfaxLibSourceRepository implements SourceRepository {
    File localDir
    List<String> projectNames = [
            'aamva-authentication-client',
       'aamva-consumer-access-client',
       'alert-log-domain',
       'amqp-configuration-domain',
       'answer-delivery-domain',
       'auction-partner-internal',
       'bbg-domain',
       'bmc-impact-service',
       'build-levels'
    ]

    @Override
    Collection<ProjectSource> init() {
        def result = []
        localDir.eachDir { File projectDir ->
            result.add(new CarfaxGradleProjectSource(projectDir))
        }
        return result
    }

    @Override
    File getLocalDir() {
        return localDir
    }

    @Override
    void setLocalDir(File localDir) {
        this.localDir = localDir
    }

    @Override
    void downloadAll() {
        if (localDir.exists()) {
            File original = new File(localDir.getCanonicalPath())
            original.renameTo(new File(localDir.parentFile, localDir.name+"-"+System.currentTimeMillis()) )

        }
        localDir.mkdirs()
        projectNames.collect() { String projectName ->

            String cmd ="cmd /c git clone ssh://git@stash:7999/lib/${projectName}.git ${localDir.absolutePath.replaceAll("\\\\", "/")+'/'+projectName}"
            println cmd
            cmd.execute()
        }.each {it.waitFor()}

    }


    @Override
    void upload(ProjectSource project) {

    }
}
