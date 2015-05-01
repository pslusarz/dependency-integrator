package org.di.engine

import org.di.api.ProjectSource

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class BuildRunner {
    List<ProjectSource> projectSources
    List<BuildRecord> buildRecords = new CopyOnWriteArrayList<>()
    ExecutorService executor
    def start(int numberOfThreads = 10) {
       buildRecords.addAll(projectSources.collect {new BuildRecord(projectSource: it)})
       executor = Executors.newFixedThreadPool(numberOfThreads)
        buildRecords.each { record ->
          record.startEpoch = System.currentTimeMillis()
          executor.execute(new BuildRun(buildRecord: record))
        }
    }


    List<BuildRecord> getCompleteBuildRecords() {
        executor.shutdown()
        while (!executor.terminated) {
            executor.awaitTermination(10, TimeUnit.SECONDS)
            println "---------------------"
            buildRecords.findAll {it.result == BuildRecord.BuildResult.Unknown}.each {
                println "Still running: "+it.projectSource.name
            }

        }
        def result = []
        result.addAll(buildRecords)
        return result
    }

    class BuildRun implements Runnable {
        BuildRecord buildRecord
        @Override
        void run() {
           boolean result = buildRecord.projectSource.build()
           buildRecord.result = result ? BuildRecord.BuildResult.Passed : BuildRecord.BuildResult.Failed
           buildRecord.stopEpoch = System.currentTimeMillis()
        }
    }
}
