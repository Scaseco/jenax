package org.aksw.jenax.arq.util.tuple.adapter;

import java.util.Collection;

import org.aksw.commons.tuple.accessor.TupleAccessor;
import org.aksw.commons.tuple.bridge.TupleBridge;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;

public class TupleBridgeBinding
    implements TupleBridge<Binding, Node>
{
    private final Var[] vars;

    protected TupleBridgeBinding(Var[] vars) {
        super();
        this.vars = vars;
    }

    @Override
    public int getDimension() {
        return vars.length;
    }

    public static TupleBridgeBinding of(Var... vars) {
        return new TupleBridgeBinding(vars);
    }

    public static TupleBridgeBinding of(Collection<Var> vars) {
        return of(vars.toArray(new Var[0]));
    }

    @Override
    public Node get(Binding tupleLike, int componentIdx) {
        Var var = vars[componentIdx];
        Node result = tupleLike.get(var);
        return result;
    }

    @Override
    public <T> Binding build(T obj, TupleAccessor<? super T, ? extends Node> accessor) {
        // Preconditions.checkArgument(getDimension() == nodes.length, "Argument length does not match the dimension.");
        BindingBuilder builder = BindingFactory.builder();
        for (int i = 0; i < getDimension(); ++i) {
            Var var = vars[i];
            Node node = accessor.get(obj, i);
            builder.add(var, node);
        }
        Binding result = builder.build();
        return result;
    }
}
