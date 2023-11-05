package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.fragment.api.Fragment3;
import org.aksw.jenax.sparql.fragment.impl.Fragment3Impl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.syntax.Element;

public class GeoFragmentUtils {
    /** Virtuoso constants - should go to a virtuoso-specific class */
    public static final String defaultIntersectsFnName = "bif:st_intersects";
    public static final String defaultGeomFromTextFnName = "bif:st_geomFromText";

}
