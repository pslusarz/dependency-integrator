package org.di.api.impl.carfax.util

class Git {
    static String getVersionTags(File projectDir) {
        String cmd = "git --git-dir=${projectDir.absolutePath}/.git --no-pager --work-tree=${projectDir.absolutePath} log --tags --grep=release --pretty=oneline"

        return Command.run(cmd)
    }
}
