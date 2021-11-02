package org.aksw.jena_sparql_api.relationlet;

import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.collectors.CollectorUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

public abstract class RelationletElementBase
    extends RelationletBaseWithMutableFixedVars
    implements RelationletSimple
{

    @Override
    public Set<Var> getVarsMentioned() {
        Element el = getElement();
        Set<Var> result = ElementUtils.getVarsMentioned(el);
        return result;
    }


    @Override
    public RelationletSimple materialize() {
        Map<Var, Var> identityMap = getVarsMentioned().stream()
                .collect(CollectorUtils.toLinkedHashMap(x -> x, x -> x));

        Element el = getElement();
        Set<Var> fixedVars = getPinnedVars();
        RelationletSimple result = new RelationletNestedImpl(el, identityMap, fixedVars);
        return result;
    }
}