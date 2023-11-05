package org.aksw.jenax.arq.util.update;

import java.util.function.Function;

import org.apache.jena.update.UpdateRequest;

@FunctionalInterface
public interface UpdateRequestTransform
    extends Function<UpdateRequest, UpdateRequest>
{
}
