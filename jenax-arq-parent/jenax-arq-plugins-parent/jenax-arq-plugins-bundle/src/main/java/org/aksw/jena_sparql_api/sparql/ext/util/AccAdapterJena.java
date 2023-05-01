package org.aksw.jena_sparql_api.sparql.ext.util;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.aggregate.Accumulator;
import org.apache.jena.sparql.function.FunctionEnv;

/** Bridge to enable use of Accumulator of aksw-commons with jena */
public class AccAdapterJena
    implements Accumulator {

    protected org.aksw.commons.collector.domain.Accumulator<Binding, FunctionEnv, NodeValue> accDelegate;

    public AccAdapterJena(org.aksw.commons.collector.domain.Accumulator<Binding, FunctionEnv, NodeValue> accDelegate) {
        super();
        this.accDelegate = accDelegate;
    }

    @Override
    public void accumulate(Binding binding, FunctionEnv functionEnv) {
        accDelegate.accumulate(binding, functionEnv);
    }

    @Override
    public NodeValue getValue() {
        NodeValue result = accDelegate.getValue();
        return result;
    }

    public org.aksw.commons.collector.domain.Accumulator<Binding, FunctionEnv, NodeValue> getAccDelegate() {
        return accDelegate;
    }
}