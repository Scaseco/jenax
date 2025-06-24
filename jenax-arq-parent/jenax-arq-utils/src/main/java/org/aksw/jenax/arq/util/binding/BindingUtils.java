package org.aksw.jenax.arq.util.binding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.aksw.jenax.arq.util.node.NodeTransformRenameMap;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingLib;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;

public class BindingUtils {

    public static Node substitute(Node node, Binding binding) {
        Node result = node;

        if (node.isVariable()) {
            result = binding.get((Var) node);
            if (result == null) {
                throw new RuntimeException("Variable " + node + "not bound");
            }
        }
        return result;
    }

    /**
     * Add a mapping to the given binding builder based on the i-th entry in a list of nodes
     * and a supplier of values.
     * If adding a binding on that basis fails then the result is null.
     * If the builder is null then the value supplier will never be invoked.
     * If the i-th node is a concrete value then the result is only non-null if the value supplier
     * yields an equivalent node.
     *
     * @param builder The binding builder. May be null.
     * @param nodes A list of non-null nodes which may be variables or concrete values.
     * @param i The index of the node/variable to use from the list of nodes
     * @param valueSupplier
     * @return
     */
    public static BindingBuilder add(BindingBuilder builder, List<Node> nodes, int i, Supplier<Node> valueSupplier) {
        BindingBuilder result = builder;
        if (builder != null) {
            int n = nodes.size();
            if (i < n) {
                Node key = nodes.get(i);
                Node value = valueSupplier.get();
                if (value != null) {
                    if (key.isVariable()) {
                        builder.add((Var)key, value);
                    } else if (!Objects.equals(key, value)) {
                        result = null;
                    }
                }
            }
        }

        return result;
    }

    /** If key is null then return null.
     *  If key is a variable then return the value in the binding (may be null) - otherwise return the key itself */
    public static Node getValue(Binding binding, Node key) {
        Node result = key == null
                ? null
                : key.isVariable()
                    ? binding.get((Var)key)
                    : key;
        return result;
    }

    /** Extends {@link #getValue(Binding, Node)} such that if it returns null then a default value is returned instead */
    public static Node getValue(Binding binding, Node key, Node defaultWhenNull) {
        Node result = getValue(binding, key);
        if (result == null) {
            result = defaultWhenNull;
        }
        return result;
    }

//    public static Binding clone(Binding binding) {
//        Binding result = new BindingHashMap();
//    }

    public static Binding project(Binding binding, Iterable<Var> vars) {
        return project(binding, vars.iterator());
    }

    public static Binding project(Binding binding, Iterator<Var> vars) {
        BindingBuilder builder = BindingBuilder.create();
        while (vars.hasNext()) {
            Var var = vars.next();
            Node node = binding.get(var);
            if (node != null) {
                builder.add(var, node);
            }
        }
        return builder.build();
    }

    public static Binding project(Binding binding, Iterator<Var> vars, Set<Var> blacklist) { // Replace with predicate?
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

    /** Similar to {@link BindingLib#bindingToMap(Binding)} but does not use a lambda */
    public static Map<Var, Node> toMap(Binding binding) {
        Map<Var, Node> result = new HashMap<>();
        Iterator<Var> it = binding.vars();
        while(it.hasNext()) {
            Var v = it.next();
            Node n = binding.get(v);
            result.put(v, n);
        }
        return result;
    }

    public static List<Binding> addRowIds(Collection<Binding> bindings, Var rowId) {
        List<Binding> result = new ArrayList<>(bindings.size());
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

    public static Number getNumberNullable(Binding binding, Node key) {
        Node node = BindingUtils.getValue(binding, key);
        Number result = NodeUtils.getNumberNullable(node);
        return result;
    }

    public static Optional<Number> tryGetNumber(Binding binding, Node key) {
        return Optional.ofNullable(getNumberNullable(binding, key));
    }

    /** Get a binding's values for var as a number. Raises an NPE if no number can be obtained */
    public static Number getNumber(Binding binding, Node key) {
        Node node = BindingUtils.getValue(binding, key);
        Number result = NodeUtils.getNumber(node);
        return result;
    }

    /** Util function for quickly creating arrays that act as join keys */
    public static void projectIntoArray(Node[] dest, int offset, Binding binding, Var[] projectVars) {
        int n = projectVars.length;
        for (int i = 0; i < n; ++i) {
            Var var = projectVars[i];
            Node node = binding.get(var);
            dest[offset + i] = node;
        }
    }

    /** Tuple is not serializable - so it doesn't work in spark */
    public static Tuple<Node> projectAsTuple(Binding binding, Var[] projectVars) {
        Node[] tmp = new Node[projectVars.length];
        projectIntoArray(tmp, 0, binding, projectVars);
        Tuple<Node> result = TupleFactory.create(tmp);
        return result;
    }

    public static List<Node> projectAsList(Binding binding, Var[] projectVars) {
        Node[] tmp = new Node[projectVars.length];
        projectIntoArray(tmp, 0, binding, projectVars);
        List<Node> result = Arrays.asList(tmp);
        return result;
    }


//    public static Number getNumberNullable(Binding binding, Var var) {
//        Node node = binding.get(var);
//        Number result = NodeUtils.getNumberNullable(node);
//        return result;
//    }
//
//    /** Get a binding's values for var as a number. Raises an NPE if no number can be obtained */
//    public static Number getNumber(Binding binding, Var var) {
//        Node node = binding.get(var);
//        Number result = NodeUtils.getNumber(node);
//        return result;
//    }
//
}
