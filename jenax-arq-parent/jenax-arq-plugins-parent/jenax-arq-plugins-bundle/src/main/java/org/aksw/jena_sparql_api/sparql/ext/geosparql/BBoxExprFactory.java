package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.apache.jena.sparql.expr.Expr;
import org.locationtech.jts.geom.Envelope;

/**
 * Interface to create sparql fragments for bounding box matching in different vocabularies
 */
public interface BBoxExprFactory {
    Expr createExpr(Envelope bbox);
}
