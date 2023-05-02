package org.aksw.jenax.arq.util.syntax;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBase;
import org.apache.jena.sparql.engine.binding.BindingLib;

/**
 * A mutable version of binding for use with {@link VarExprListUtils#eval(org.apache.jena.sparql.core.VarExprList, Binding, org.apache.jena.sparql.function.FunctionEnv)}
 * Profiling suggested that repeatedly using {@link org.apache.jena.sparql.engine.binding.BindingBuilder#snapshot()} performs worse than evaluating against a mutable binding.
 */
public class BindingOverMapMutable extends BindingBase {

    protected final Map<Var, Node> map;

    public BindingOverMapMutable(Binding parent) {
    	this(parent, new HashMap<>());
    }

    public BindingOverMapMutable(Binding parent, Map<Var, Node> map) {
        super(parent);
        this.map = map;
    }

    public static BindingOverMapMutable copyOf(Binding parent) {
    	return new  BindingOverMapMutable(null, BindingLib.bindingToMap(parent));
    }
    
    public void add(Var var, Node node) {
    	map.put(var, node);
    }
    
    @Override
    protected Iterator<Var> vars1() {
        return Iter.noRemove(map.keySet().iterator());
    }

    @Override
    protected boolean contains1(Var var) {
        return map.containsKey(var);
    }

    @Override
    protected Node get1(Var var) {
        return map.get(var);
    }

    @Override
    protected int size1() {
        return map.size();
    }

    @Override
    protected boolean isEmpty1() {
        return map.isEmpty();
    }
}
