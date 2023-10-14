package org.aksw.jenax.analytics.core;

import java.util.HashMap;
import java.util.Map;

import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.syntax.Template;

public class ObjectQueryImpl
    extends ObjectQueryBase
{
    protected Template template;
    protected Fragment relation;

    public ObjectQueryImpl(Template template, Fragment relation) {
        this(template, relation, new HashMap<>());
    }

    public ObjectQueryImpl(Template template, Fragment relation, Map<Node, ExprList> idMapping) {
        super(idMapping);
        this.template = template;
        this.relation = relation;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public Fragment getRelation() {
        return relation;
    }

    public void setRelation(Fragment relation) {
        this.relation = relation;
    }

    @Override
    public String toString() {
        return "ObjectQueryImpl [template=" + template + ", relation=" + relation + ", idMapping=" + idMapping + "]";
    }
}

