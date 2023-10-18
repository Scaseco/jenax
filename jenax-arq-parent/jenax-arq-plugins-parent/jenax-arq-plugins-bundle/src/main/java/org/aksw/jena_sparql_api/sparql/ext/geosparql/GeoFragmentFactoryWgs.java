package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.api.Fragment3;
import org.aksw.jenax.sparql.fragment.impl.Fragment3Impl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;
import org.locationtech.jts.geom.Envelope;

public class GeoFragmentFactoryWgs
{
    // The fragment is typically of the form ?s { ?s wgs:lat ?lat ; wgs:long ?long }
    protected Fragment fragment;
    protected Var xVar;
    protected Var yVar;
    protected Node castNode;

    protected GeoFragmentFactoryWgs(Fragment fragment, Var xVar, Var yVar, Node castNode) {
        this.fragment = fragment;
        this.xVar = xVar;
        this.yVar = yVar;
        this.castNode = castNode;
    }

    public static GeoFragmentFactoryWgs of(Var xVar, Var yVar) {
        return of(xVar, yVar, null);
    }

    public static GeoFragmentFactoryWgs of(Var xVar, Var yVar, Node castNode) {
        return new GeoFragmentFactoryWgs(xVar, yVar, castNode);
    }

    public Fragment getFragment() {
        return fragment;
    }

    public Var getxVar() {
        return xVar;
    }

    public Var getyVar() {
        return yVar;
    }

    public Node getCastNode() {
        return castNode;
    }

    @Override
    public Expr createExpr(Envelope bounds) {
        Expr result = GeoExprUtils.createExprWgs84Intersects(xVar, yVar, bounds, castNode);
        return result;
    }

    public static Fragment3 createLatLonFragment(Node xProperty, Node yProperty) {
        Element elt = ElementUtils.createElementTriple(
            Triple.create(Vars.s, xProperty, Vars.x),
            Triple.create(Vars.s, yProperty, Vars.y));
        Fragment3 result = new Fragment3Impl(elt, Vars.s, Vars.x, Vars.y);
        return result;
    }

    public static Fragment3 createLatLonFragmentWgs() {
        return createLatLonFragment(WGS84.xlong.asNode(), WGS84.lat.asNode());
    }
}

