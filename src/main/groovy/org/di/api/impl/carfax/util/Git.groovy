package org.di.api.impl.carfax.util

class Git {
    static String getVersionTags(File projectDir) {
        String cmd = "git --git-dir=${projectDir.absolutePath}/.git --no-pager --work-tree=${projectDir.absolutePath} log --tags --grep=release --pretty=oneline"

        return Command.run(cmd)
    }

    static void checkout(File projectDir, String commitSha = "master") {
        String cmd1 = "git --git-dir=${projectDir.absolutePath}/.git --work-tree=${projectDir.absolutePath} -C ${projectDir.absolutePath} stash"
        println " >>>> git: "+cmd1
        println " >>> stashing..."+Command.run(cmd1)
        String cmd = "git --git-dir=${projectDir.absolutePath}/.git --work-tree=${projectDir.absolutePath} checkout ${commitSha}"
        println " >>> checking out.... "+Command.run(cmd)
    }
}
