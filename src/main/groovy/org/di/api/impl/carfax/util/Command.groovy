package org.di.api.impl.carfax.util

import groovy.util.logging.Log

@Log
class Command {
    static String commandExecutionPrefix
    static {
       commandExecutionPrefix = isWindows() ? "cmd /c " : ""
    }

    static boolean isWindows() {
        System.getProperty("os.name").startsWith("Windows")
    }

    static String run(String command) {
        println "git command: "+command
        if (isWindows()) {
            command = command.replaceAll("\\\\", "/")
        }
        println "git command: "+command
        def proc = (commandExecutionPrefix+command).execute()
        def out = new StringBuffer()
        def err = new StringBuffer()
        proc.consumeProcessOutput( out, err )
        proc.waitFor()
        def result =  proc.out.toString()
        log.fine result
        def errors = proc.err.toString()
        if (errors) {
            log.warning errors
        }
        return result
    }
}
