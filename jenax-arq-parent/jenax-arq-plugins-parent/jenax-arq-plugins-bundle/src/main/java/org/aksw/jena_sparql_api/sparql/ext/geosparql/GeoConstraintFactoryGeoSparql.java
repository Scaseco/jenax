package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import java.util.Objects;

import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.aksw.jenax.sparql.fragment.impl.Fragment2Impl;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;
import org.locationtech.jts.geom.Envelope;

public class GeoConstraintFactoryGeoSparql
    implements GeoConstraintFactory
{
    // The fragment is typically of the form ?s { ?s wgs:lat ?lat ; wgs:long ?long }
    protected Fragment2 fragment;
    protected BBoxExprFactoryWkt bboxExprFactory;

    protected GeoConstraintFactoryGeoSparql(Fragment2 fragment, BBoxExprFactoryWkt bboxExprFactory) {
        // this.fragment = fragment;
        this.fragment = Objects.requireNonNull(fragment);
        this.bboxExprFactory = Objects.requireNonNull(bboxExprFactory);
    }

    public static GeoConstraintFactoryGeoSparql of(String hasGeometryPropertyIri, String asLiteralPropertyIri) {
        Node hasGeometryProperty = NodeFactory.createURI(hasGeometryPropertyIri);
        Node asLiteralProperty = NodeFactory.createURI(asLiteralPropertyIri);
        return of(hasGeometryProperty, asLiteralProperty);
    }

    public static GeoConstraintFactoryGeoSparql create() {
        return of(Geo.HAS_GEOMETRY_NODE, Geo.AS_WKT_NODE);
    }

    public static GeoConstraintFactoryGeoSparql create2() {
        return of(createFragment(Geo.AS_WKT_NODE));
    }

    public static GeoConstraintFactoryGeoSparql create(String p) {
        return create(NodeFactory.createURI(p));
    }
    public static GeoConstraintFactoryGeoSparql create(Node p) {
        return of(createFragment(p));
    }


    public static GeoConstraintFactoryGeoSparql of(Node hasGeometryProperty, Node asLiteralProperty) {
        Fragment2 fragment = createFragment(hasGeometryProperty, asLiteralProperty);
        return of(fragment);
    }

    public static GeoConstraintFactoryGeoSparql of(Fragment2 fragment) {
        return new GeoConstraintFactoryGeoSparql(fragment, BBoxExprFactoryWkt.ofGeoSparql());
    }

    @Override
    public Var getIdVar() {
        return fragment.getSourceVar();
    }

    @Override
    public Fragment2 getFragment() {
        return fragment;
    }

    @Override
    public Expr createExpr(Envelope bounds) {
        Var geomVar = fragment.getTargetVar();
        Expr result = bboxExprFactory.createExpr(geomVar, bounds);
        return result;
    }

    /**
     * Convert a binding into a WKT string
     * @implNote
     *   Currently does not validate the result.
     */
    @Override
    public String toWktString(Binding binding) {
        Var geoVar = fragment.getTargetVar();
        Node geo = binding.get(geoVar);
        GeometryWrapper geomWrapper = GeometryWrapper.extract(geo);
        String result = WKTDatatype.INSTANCE.unparse(geomWrapper);
        // String result = geo.getLiteralLexicalForm();
        return result;
    }

    public static Fragment2 createFragment(Node asLiteralProperty) {
        Element elt = ElementUtils.createElementTriple(
            Triple.create(Vars.s, asLiteralProperty, Vars.g));
        Fragment2 result = new Fragment2Impl(elt, Vars.s, Vars.g);
        return result;
    }

    public static Fragment2 createFragment(Node hasGeometryProperty, Node asLiteralProperty) {
        Element elt = ElementUtils.createElementTriple(
            Triple.create(Vars.s, hasGeometryProperty, Vars.x),
            Triple.create(Vars.s, asLiteralProperty, Vars.g));
        Fragment2 result = new Fragment2Impl(elt, Vars.s, Vars.g);
        return result;
    }
}
