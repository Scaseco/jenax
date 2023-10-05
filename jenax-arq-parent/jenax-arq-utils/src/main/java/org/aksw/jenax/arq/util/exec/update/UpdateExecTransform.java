package org.aksw.jenax.arq.util.exec.update;

import java.util.function.Function;

import org.apache.jena.update.UpdateExecution;

@FunctionalInterface
public interface UpdateExecTransform
    extends Function<UpdateExecution, UpdateExecution>
{
}
