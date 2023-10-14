package org.aksw.jenax.sparql.fragment.api;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;

/** A relation with an additional mapping of variables to custom objects */
public class MappedFragment<T>
    extends FragmentWrapperBase
{
    protected Map<Var, T> mapping;

    protected MappedFragment(Fragment delegate, Map<Var, T> mapping) {
        super(delegate);
        this.mapping = mapping;
    }

    public static <T> MappedFragment<T> of(Fragment delegate, Map<Var, T> mapping) {
        return new MappedFragment<>(delegate, mapping);
    }

    public Map<Var, T> getMapping() {
        return mapping;
    }

    @Override
    public Fragment applyNodeTransform(NodeTransform nodeTransform) {
        Fragment newDelegate = getDelegate().applyNodeTransform(nodeTransform);
        Map<Var, T> newMapping = mapping.entrySet().stream()
            .map(e -> {
                Node newVar = nodeTransform.apply(e.getKey());
                return new SimpleEntry<>((Var)newVar, e.getValue());
            })
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        return new MappedFragment<>(newDelegate, newMapping);
    }
}
