package org.di.api.impl.carfax.util


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
        String result =  proc.in.text
        println result
        println proc.err.text
        return result
    }
}
