package org.di.api.impl.carfax.util

import groovy.util.logging.Log

@Log
class Git {
    static String getVersionTags(File projectDir) {
        String cmd = "git --git-dir=${projectDir.absolutePath}/.git --no-pager --work-tree=${projectDir.absolutePath} log --tags --grep=release --pretty=oneline"

        return Command.run(cmd)
    }

    static void checkout(File projectDir, String commitSha = "master") {
        String cmd1 = "git --git-dir=${projectDir.absolutePath}/.git --work-tree=${projectDir.absolutePath} -C ${projectDir.absolutePath} stash"
        log.fine " >>>> git: "+cmd1
        log.fine " >>> stashing..."+Command.run(cmd1)
        String cmd = "git --git-dir=${projectDir.absolutePath}/.git --work-tree=${projectDir.absolutePath} checkout ${commitSha}"
        log.fine " >>> checking out.... "+Command.run(cmd)
    }
}
