package org.aksw.shellgebra.unused.algebra.plan;

public class CommandEnv {
    // protected CommandLine commandLine;
    // protected String toolName;
    // protected String resolvedCommand;
    String toolName;
    String commandName;
    String commandTarget;
    // String[] commandArgs;

    public CommandEnv(String toolName, String commandName, String commandTarget) {
        super();
        this.toolName = toolName;
        this.commandName = commandName;
        this.commandTarget = commandTarget;

        // this.toolName = toolName;
        // this.resolvedCommand = resolvedCommand;
    }


//    public CommandEnv(CommandLine commandLine) {
//        super();
//        this.commandLine = commandLine;
//    }

//    public CommandLine getCommandLine() {
//        return commandLine;
//    }
}
