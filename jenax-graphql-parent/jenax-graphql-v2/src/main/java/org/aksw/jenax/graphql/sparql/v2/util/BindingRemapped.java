package org.aksw.jenax.graphql.sparql.v2.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;


/**
 * A view over a binding. The variable maps the exposed variables to that of the bindings.
 * This class is useful in situations where the original variables were remapped to internal ones, and an internal
 * binding should appear as an original one to the application.
 */
public class BindingRemapped
    implements Binding
{
    protected Binding delegate;
    protected Map<Var, Var> varMap;

    // Computed when needed
    protected transient Set<Var> effectiveKeys;

    protected BindingRemapped(Binding delegate, Map<Var, Var> varMap) {
        super();
        this.delegate = Objects.requireNonNull(delegate);
        this.varMap = Objects.requireNonNull(varMap);
    }

    public static Binding of(Binding delegate, Map<Var, Var> varMap) {
        return new BindingRemapped(delegate, varMap);
    }

    protected Set<Var> effectiveKeys() {
        if (effectiveKeys == null) {
            effectiveKeys= varMap.keySet().stream().filter(delegate::contains).collect(Collectors.toSet());
        }
        return effectiveKeys;
    }

    @Override
    public Iterator<Var> vars() {
        return varMap.keySet().stream().filter(delegate::contains).iterator();
    }

    @Override
    public Set<Var> varsMentioned() {
        return effectiveKeys();
    }

    @Override
    public void forEach(BiConsumer<Var, Node> action) {
        varMap.forEach((v, w) -> {
            if (delegate.contains(w)) {
                action.accept(v, delegate.get(w));
            }
        });
    }

    @Override
    public boolean contains(Var var) {
        Var v = varMap.get(var);
        return delegate.contains(v);
    }

    /**
     * Maps the argument variable to the internal one.
     * Then uses the internal one for the lookup.
     */
    @Override
    public Node get(Var var) {
        Var v = varMap.get(var);
        Node result = v == null ? null : delegate.get(v);
        return result;
    }

    @Override
    public int size() {
        return (int)varMap.keySet().stream().filter(delegate::contains).count();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public String toString() {
        return delegate.toString() + ": " + varMap;
    }
}
