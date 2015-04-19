package org.di.engine

import org.di.api.ProjectSource

import java.util.concurrent.CopyOnWriteArrayList

class BuildRunner {
    List<ProjectSource> projectSources
    List<BuildRecord> buildRecords = new CopyOnWriteArrayList<>()
    def start() {
       buildRecords.addAll(projectSources.collect {new BuildRecord(projectSource: it)})
        buildRecords.each { record ->
          record.startEpoch = System.currentTimeMillis()
          record.result = record.projectSource.build() ? BuildRecord.BuildResult.Passed : BuildRecord.BuildResult.Failed
          record.stopEpoch = System.currentTimeMillis()
        }
    }


    List<BuildRecord> getCompleteBuildRecords() {
        def result = []
        result.addAll(buildRecords)
        return result
    }
}
