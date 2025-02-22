package org.aksw.jenax.arq.util.op;

import java.util.function.Function;

import org.apache.jena.sparql.algebra.Op;

@FunctionalInterface
public interface OpTransform
    extends Function<Op, Op>
{
}
