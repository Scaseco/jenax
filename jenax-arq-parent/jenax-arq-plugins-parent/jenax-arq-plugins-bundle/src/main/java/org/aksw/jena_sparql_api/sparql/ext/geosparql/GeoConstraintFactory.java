package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.locationtech.jts.geom.Envelope;

/**
 * This class bundles
 * (a) a SPARQL graph pattern that relates entities to spatial objects with
 * (b) with a method that can constrain the rows to arbitrary bounding boxes.
 * Also, (c) there is a method to map bindings from the graph pattern's result
 * set to WKT strings.
 *
 * The 'factory' aspect refers to the {@link #createExpr(Envelope)} method which is a factory
 * for SPARQL expressions from bounding boxes.
 */
public interface GeoConstraintFactory {
    /** Return a fragment which returns geometry information for an entity */
    Fragment getFragment();

    /** Return which of the fragment's variables is the id variable */
    Var getIdVar();

    /** Given a binding from the fragment create a WKT string from it */
    String toWktString(Binding binding);

    /** Create a SPARQL expression that constrains the fragment to the given bounding box */
    Expr createExpr(Envelope bounds);
}
