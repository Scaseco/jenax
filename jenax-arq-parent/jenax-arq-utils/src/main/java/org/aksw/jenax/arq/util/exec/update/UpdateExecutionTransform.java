package org.aksw.jenax.arq.util.exec.update;

import java.util.function.Function;

import org.apache.jena.sparql.exec.UpdateExec;

@FunctionalInterface
public interface UpdateExecutionTransform
    extends Function<UpdateExec, UpdateExec>
{
}
