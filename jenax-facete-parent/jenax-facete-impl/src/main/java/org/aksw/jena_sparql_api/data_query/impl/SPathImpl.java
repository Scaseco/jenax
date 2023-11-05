package org.aksw.jena_sparql_api.data_query.impl;

import org.aksw.jena_sparql_api.data_query.api.SPath;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.aksw.jenax.sparql.fragment.impl.Fragment2Impl;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;

public class SPathImpl
    extends SelectionImpl
    implements SPath
{
    public SPathImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    @Override
    public SPath getParent() {
        SPath result = ResourceUtils.getPropertyValue(this, RDF.first, SPath.class);

        return result;
    }

//	@Override
    public void setParent(Resource parent) {
        ResourceUtils.setProperty(this, RDF.first, parent);
    }

    @Override
    //Property
    public String getPredicate() {
        Property p = ResourceUtils.getPropertyValue(this, RDF.predicate, Property.class);
        String result = p == null ? null : p.getURI();
        return result;
    }

    @Override
    public boolean isReverse() {
        boolean result = ResourceUtils.tryGetLiteralPropertyValue(this, RDF.rest, Boolean.class).orElse(false);
        return result;
    }

    @Override
    public SPath get(String p, boolean reverse) {
        SPathImpl result = (SPathImpl)getModel().createResource().as(SPath.class);
        result.setParent(this);
        result.setPredicate(p);
        result.setReverse(reverse);
        return result;
    }

//	@Override
    public void setPredicate(Property p) {
        ResourceUtils.setProperty(this, RDF.predicate, p);
    }
    public void setPredicate(String p) {
        //ResourceUtils.setProperty(this, RDF.predicate, this.getModel().createProperty(p));
        setPredicate(ResourceFactory.createProperty(p));
    }

//	@Override
    public void setReverse(boolean isReverse) {
        ResourceUtils.setLiteralProperty(this, RDF.rest, isReverse ? true : null);
    }

    @Override
    public Fragment2 getReachingBinaryRelation() {
        boolean isReverse = isReverse();
        //Node p = getPredicate().asNode();
        String pStr = getPredicate();
        Node p = NodeFactory.createURI(pStr);
        Fragment2 result = new Fragment2Impl(ElementUtils.createElement(QueryFragment.createTriple(isReverse, Vars.s, p, Vars.o)), Vars.s, Vars.o);
        return result;
    }

}
