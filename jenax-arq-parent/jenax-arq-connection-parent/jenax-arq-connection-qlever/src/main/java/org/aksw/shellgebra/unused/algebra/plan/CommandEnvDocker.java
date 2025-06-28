package org.aksw.shellgebra.unused.algebra.plan;

import org.aksw.shellgebra.docker.adapter.cli.ContainerDef;

import to_delete.CommandEnv;

/**
 * Execution of a command using a docker container.
 */
public class CommandEnvDocker
    extends CommandEnv
{
    protected ContainerDef containerDef;

    public CommandEnvDocker(String toolName, String commandName, String commandTarget, String[] commandArgs, ContainerDef conatinerDef) {
        super(toolName, commandName, commandTarget);
        this.containerDef = conatinerDef;
    }

    public ContainerDef getContainerDef() {
        return containerDef;
    }
}
