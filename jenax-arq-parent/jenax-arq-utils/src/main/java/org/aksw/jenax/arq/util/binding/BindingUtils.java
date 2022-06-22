package org.aksw.jenax.arq.util.binding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jenax.arq.util.node.NodeTransformRenameMap;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;

public class BindingUtils {

    public static Binding project(Binding binding, Iterable<Var> vars) {
        return project(binding, vars.iterator(), Collections.emptySet());
    }

    public static Binding project(Binding binding, Iterator<Var> vars, Set<Var> blacklist) {
        BindingBuilder builder = BindingBuilder.create();

        while (vars.hasNext()) {
            Var var = vars.next();
            if (!blacklist.contains(var)) {
                Node node = binding.get(var);
                if (node != null) {
                    builder.add(var, node);
                }
            }
        }

        return builder.build();
    }

    public static Binding fromMap(Map<? extends Var, ? extends Node> map) {
        BindingBuilder builder = BindingBuilder.create();
        for(Entry<? extends Var, ? extends Node> e : map.entrySet()) {
            builder.add(e.getKey(), e.getValue());
        }
        return builder.build();
    }

    public static Binding transformKeys(Binding binding, NodeTransform transform) {
        Iterator<Var> it = binding.vars();

        BindingBuilder builder = BindingBuilder.create();
        while(it.hasNext()) {
            Var o = it.next();
            Node node = binding.get(o);

            Var n = (Var)transform.apply(o);

            builder.add(n, node);
        }

        return builder.build();
    }

    public static Map<Var, Node> toMap(Binding binding) {
        Map<Var, Node> result = new HashMap<Var, Node>();
        Iterator<Var> it = binding.vars();
        while(it.hasNext()) {
            Var v = it.next();
            Node n = binding.get(v);
            result.put(v, n);
        }

        return result;
    }

    public static List<Binding> addRowIds(Collection<Binding> bindings, Var rowId) {
        List<Binding> result = new ArrayList<Binding>(bindings.size());
        long i = 0;
        BindingBuilder builder = BindingBuilder.create();
        for(Binding parent : bindings) {
            builder.reset();
            builder.addAll(parent);
            Node node = NodeValue.makeInteger(i).asNode();
            builder.add(rowId, node);
            ++i;
            result.add(builder.build());
        }

        return result;
    }

    public static Binding renameKeys(Binding binding, Map<Var, Var> varMap) {
        return NodeTransformLib.transform(binding, NodeTransformRenameMap.create(varMap));
    }

    public static NodeTransform asNodeTransform(Binding binding) {
        return new NodeTransformSubst(new MapFromBinding(binding));
    }


    public static Number getNumberNullable(Binding binding, Var var) {
        Node node = binding.get(var);
        Number result = NodeUtils.getNumberNullable(node);
        return result;
    }

    /** Get a binding's values for var as a number. Raises an NPE if no number can be obtained */
    public static Number getNumber(Binding binding, Var var) {
        Node node = binding.get(var);
        Number result = NodeUtils.getNumber(node);
        return result;
    }

}
