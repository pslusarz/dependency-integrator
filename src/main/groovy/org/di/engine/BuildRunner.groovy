package org.di.engine

import org.di.api.ProjectSource

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class BuildRunner {
    Collection<ProjectSource> projectSources
    List<BuildRecord> buildRecords = new CopyOnWriteArrayList<>()
    ExecutorService executor
    def start(int numberOfThreads = 10) {
       buildRecords.addAll(projectSources.collect {new BuildRecord(projectSource: it)})
       executor = Executors.newFixedThreadPool(numberOfThreads)
        buildRecords.each { record ->
          record.startEpoch = System.currentTimeMillis()
          executor.execute(new BuildRun(buildRecord: record))
        }
        return this
    }


    List<BuildRecord> getCompleteBuildRecords() {
        executor.shutdown()
        while (!executor.terminated) {
            executor.awaitTermination(10, TimeUnit.SECONDS)
            println "---------------------"
            def running = buildRecords.findAll {it.result == BuildRecord.BuildResult.Unknown}.collect{it.projectSource.name}
            def passed = buildRecords.findAll {it.result == BuildRecord.BuildResult.Passed}.collect{it.projectSource.name}
            def failed = buildRecords.findAll {it.result == BuildRecord.BuildResult.Failed}.collect{it.projectSource.name}
            println "Passed: ${passed.size()}, Failed: ${failed.size()}, Running: ${running.size()}"
            println "    Failed: "+failed
            println "   Running: "+running

        }
        def result = []
        result.addAll(buildRecords)
        return result
    }

    class BuildRun implements Runnable {
        BuildRecord buildRecord
        @Override
        void run() {
           boolean result = false
            try {
                result = buildRecord.projectSource.build()
            } finally {
                buildRecord.result = result ? BuildRecord.BuildResult.Passed : BuildRecord.BuildResult.Failed
                buildRecord.stopEpoch = System.currentTimeMillis()
            }
        }
    }
}
