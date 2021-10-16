package org.aksw.jenax.arq.util.binding;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.ext.com.google.common.base.Objects;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

/** An immutable map view over a binding. With jena4 bindings are now always immutable. */
public class MapFromBinding
    extends AbstractMap<Var, Node>
{
    protected Binding binding;

    @Override
    public Node get(Object key) {
        Node result = key instanceof Var
                ? binding.get((Var)key)
                : null;

        return result;
    }


    @Override
    public boolean containsKey(Object key) {
        boolean result = key instanceof Var
                ? binding.contains((Var)key)
                : false;

        return result;
    }

    @Override
    public Set<Entry<Var, Node>> entrySet() {
        return new AbstractSet<Entry<Var, Node>>() {

            @Override
            public boolean contains(Object o) {
                boolean result = false;
                if (o instanceof Entry) {
                    Entry<?, ?> e = (Entry<?, ?>)o;

                    Object k = e.getKey();
                    if (MapFromBinding.this.containsKey(k)) {
                        Object mapV = MapFromBinding.this.get(k);
                        result = Objects.equal(mapV, e.getValue());
                    }
                }
                return result;
            }

            @Override
            public Iterator<Entry<Var, Node>> iterator() {
                return Streams.stream(binding.vars())
                        .map(v -> (Entry<Var, Node>)new SimpleEntry<>(v, binding.get(v)))
                        .iterator();
            }

            @Override
            public int size() {
                return binding.size();
            }
        };
    }

}
