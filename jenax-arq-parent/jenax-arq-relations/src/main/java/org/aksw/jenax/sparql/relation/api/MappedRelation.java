package org.aksw.jenax.sparql.relation.api;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;

/** A relation with an additional mapping of variables to custom objects */
public class MappedRelation<T>
    extends RelationWrapperBase
{
    protected Map<Var, T> mapping;

    protected MappedRelation(Relation delegate, Map<Var, T> mapping) {
        super(delegate);
        this.mapping = mapping;
    }

    public static <T> MappedRelation<T> of(Relation delegate, Map<Var, T> mapping) {
        return new MappedRelation<>(delegate, mapping);
    }

    public Map<Var, T> getMapping() {
        return mapping;
    }

    @Override
    public Relation applyNodeTransform(NodeTransform nodeTransform) {
        Relation newDelegate = getDelegate().applyNodeTransform(nodeTransform);
        Map<Var, T> newMapping = mapping.entrySet().stream()
            .map(e -> {
                Node newVar = nodeTransform.apply(e.getKey());
                return new SimpleEntry<>((Var)newVar, e.getValue());
            })
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        return new MappedRelation<>(newDelegate, newMapping);
    }
}
