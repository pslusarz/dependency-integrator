package org.di.engine

import org.di.api.ProjectSource


class BuildRecord {
    ProjectSource projectSource
    //public enum BuildStatus {NotStarted, Running, Finished}
    public enum BuildResult {Passed, Failed, Unknown}

    BuildResult result = BuildResult.Unknown
    //BuildStatus status = BuildStatus.NotStarted
    long startEpoch = -1
    long stopEpoch = -1

    def getRunTimeMillis() {
        stopEpoch - startEpoch
    }

}
