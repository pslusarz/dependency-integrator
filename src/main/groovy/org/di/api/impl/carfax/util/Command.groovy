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
        def proc = (commandExecutionPrefix+command).execute()
        proc.waitFor()
        def result =  proc.in.text
        log.fine result
        def errors = proc.err.text
        if (errors) {
            log.warning errors
        }
        return result
    }
}
