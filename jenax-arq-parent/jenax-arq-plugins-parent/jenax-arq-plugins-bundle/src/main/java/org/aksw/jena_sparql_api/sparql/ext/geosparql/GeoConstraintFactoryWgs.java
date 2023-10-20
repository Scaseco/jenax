package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.fragment.api.Fragment3;
import org.aksw.jenax.sparql.fragment.impl.Fragment3Impl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.vocabulary.XSD;
import org.locationtech.jts.geom.Envelope;

/**
 * A bridge between a SPARQL relation that can be constrained to a bounding box
 * and converting matching rows to WKT strings.
 * (Could be extended to also expose JTS geometries and Jena's GeometryWrappers)
 */
public class GeoConstraintFactoryWgs
    implements GeoConstraintFactory
{
    // The fragment is typically of the form ?s { ?s wgs:lat ?lat ; wgs:long ?long }
    protected Fragment3 fragment;
    protected Node castNode;

    protected GeoConstraintFactoryWgs(Fragment3 fragment, Node castNode) {
        // this.fragment = fragment;
        this.fragment = fragment;
        this.castNode = castNode;
    }

    public static GeoConstraintFactoryWgs of(Node xProperty, Node yProperty, Node castNode) {
        Fragment3 fragment = createXyFragment(xProperty, yProperty);
        return new GeoConstraintFactoryWgs(fragment, castNode);
    }

    public static GeoConstraintFactoryWgs create() {
        return of(WGS84.xlong.asNode(), WGS84.lat.asNode(), XSD.xfloat.asNode());
    }

    @Override
    public Fragment3 getFragment() {
        return fragment;
    }

    @Override
    public Var getIdVar() {
        return fragment.getS();
    }

    public Var getXVar() {
        return fragment.getP();
    }

    public Var getYVar() {
        return fragment.getO();
    }

    public Node getCastNode() {
        return castNode;
    }

    @Override
    public Expr createExpr(Envelope bounds) {
        Var xVar = getXVar();
        Var yVar = getYVar();
        Expr result = GeoExprUtils.createExprWgs84Intersects(xVar, yVar, bounds, castNode);
        return result;
    }

    /** Convert a binding into a WKT string */
    @Override
    public String toWktString(Binding binding) {
        Var xVar = getXVar();
        Var yVar = getYVar();
        Node x = binding.get(xVar);
        Node y = binding.get(yVar);
        float vx = NodeValue.makeNode(x).getFloat();
        float vy = NodeValue.makeNode(y).getFloat();
        return "POINT (" + vx + " " + vy + ")";
    }

    public static Fragment3 createXyFragment(Node xProperty, Node yProperty) {
        // Cast is done when adding a bbox constraint expression
        Element elt = ElementUtils.createElementTriple(
            Triple.create(Vars.s, xProperty, Vars.x),
            Triple.create(Vars.s, yProperty, Vars.y));
        Fragment3 result = new Fragment3Impl(elt, Vars.s, Vars.x, Vars.y);
        return result;
    }

//    public static Fragment3 createLatLonFragmentWgs() {
//        return createXyFragment(WGS84.xlong.asNode(), WGS84.lat.asNode());
//    }
}


//public static GeoFragmentFactoryWgs of(Var xVar, Var yVar) {
//return of(xVar, yVar, null);
//}
//
//public static GeoFragmentFactoryWgs of(Var xVar, Var yVar, Node castNode) {
//return new GeoFragmentFactoryWgs(xVar, yVar, castNode);
//}

//Var rawX = castNode == null ? Vars.x : Var.alloc("rawX");
//Var rawY = castNode == null ? Vars.y : Var.alloc("rawY");
//if (castNode != null) {
//	new ElementBind(Vars.x, new E_Function(castNode.getURI(), new ExprVar(rawX)));
//	new ElementBind(Vars.y, new E_Function(castNode.getURI(), new ExprVar(rawY)));
//}


