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
        def out = new StringBuffer()
        def err = new StringBuffer()
        proc.consumeProcessOutput( out, err )
        proc.waitFor()
        def result =  out.toString()
        log.fine result
        def errors = err.toString()
        if (errors) {
            log.warning "Command '${command}' generated error output: "+ errors
        }
        return result
    }
}
