package org.di.api.impl.carfax.util

import groovy.util.logging.Log
import org.di.api.Version
import org.di.api.impl.carfax.StringMajorMinorPatchVersion

@Log
class GitVersionTag {
    StringMajorMinorPatchVersion version
    String origLine
    String commitSha

    static List<GitVersionTag> parseFromGitLog(String gitLog) {
        List<Version> result = []
        List<GitVersionTag> results = []
        gitLog.split("\n").findAll{it.contains " Tagging "}.each { String line ->
            def version = new StringMajorMinorPatchVersion(line.split(" ")[-3]- "-SNAPSHOT")
            if (!result.contains(version)) {
                result << version
                results << new GitVersionTag(version: version, commitSha: line.split(" ")[0], origLine: line)
            } else {
                log.warning "  duplicate version in git log: "+line
            }
        }
        return results.reverse()
    }
}
