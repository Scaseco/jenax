package org.aksw.jena_sparql_api.sparql.ext.util;

import org.aksw.commons.collector.domain.Aggregator;
import org.aksw.jena_sparql_api.sparql.ext.util.AccAdapterJena;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.aggregate.Accumulator;
import org.apache.jena.sparql.expr.aggregate.AggCustom;
import org.apache.jena.sparql.function.FunctionEnv;

/**
 * Wrapper for to use our (map-reduce-suited) {@link Aggregator} framework with Jena.
 *
 * @author raven
 *
 */
public class AggCustomWrapper
    extends AggCustom
{
    protected org.aksw.commons.collector.domain.Aggregator<Binding, FunctionEnv, NodeValue> aggDelegate;

    public AggCustomWrapper(String iri, boolean distinct, ExprList exprs) {
        super(iri, distinct, exprs);
    }

    @Override
    public Accumulator createAccumulator() {
        return new AccAdapterJena(aggDelegate.createAccumulator());
    }

}
