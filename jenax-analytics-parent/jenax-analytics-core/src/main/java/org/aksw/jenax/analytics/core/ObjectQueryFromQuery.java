package org.aksw.jenax.analytics.core;

import java.util.HashMap;
import java.util.Map;

import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.impl.FragmentUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.syntax.Template;

public class ObjectQueryFromQuery
    extends ObjectQueryBase
{
    protected Query query;

    public ObjectQueryFromQuery(Query query) {
        this(query, new HashMap<>());
    }

    public ObjectQueryFromQuery(Query query, Map<Node, ExprList> idMapping) {
        super(idMapping);
        this.query = query;
    }

    @Override
    public Template getTemplate() {
        return query.getConstructTemplate();
    }

    @Override
    public Fragment getRelation() {
        // return RelationUtils.fromQuery(query);
        Query asSelect = query.cloneQuery();
        asSelect.setQuerySelectType();
        return FragmentUtils.fromQuery(asSelect);
    }
}
