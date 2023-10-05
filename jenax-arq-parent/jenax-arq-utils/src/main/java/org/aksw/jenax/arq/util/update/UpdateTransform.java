package org.aksw.jenax.arq.util.update;

import java.util.function.Function;

import org.apache.jena.update.Update;

@FunctionalInterface
public interface UpdateTransform
    extends Function<Update, Update>
{
}
