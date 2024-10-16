package org.aksw.jenax.dataaccess.sparql.builder.exec.update;

import java.util.function.Function;

import org.apache.jena.sparql.exec.UpdateExecBuilder;

public interface UpdateExecBuilderTransform
    extends Function<UpdateExecBuilder, UpdateExecBuilder>
{
}
