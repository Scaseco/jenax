package org.aksw.facete.v3.api;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngines;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

/**
 *
 * A note on constraints:
 * Constraints can be distinguished by what they affect:
 * - NodeConstraint - a constraint on a BgpNode; disjunctive
 * - MultiNodeConstraint - a constraint on a BgpMultiNode; conjunctive
 * - GlobalConstraint - An arbitrary constraint, possibly affecting multiple paths
 *
 *
 * @author Claus Stadler, Sep 17, 2018
 *
 */
public interface FacetedQuery
    extends Castable
{
    FacetNode root();

    // If we expose a root setter on this interface, it must be a high level operation
    // that leaves the query in a consistent state - i.e. this method would have
    // to be equivalent to node.chRoot();
    //FacetNode root(FacetNode newRoot);

//	SPath getFocus();
//	void setFocus(SPath path);

    FacetNode focus();
    void focus(FacetNode node);

    Concept toConcept();

    Collection<FacetConstraint> constraints();

    FacetedQuery baseConcept(Supplier<? extends UnaryRelation> conceptSupplier);
    FacetedQuery baseConcept(UnaryRelation concept);

    UnaryRelation baseConcept();

    FacetedQuery dataSource(RdfDataSource rdfDataSource);
    RdfDataSource dataSource();

    @Deprecated
    default SparqlQueryConnection connection() {
        RdfDataSource dataSource = dataSource();
        SparqlQueryConnection result = dataSource == null
                ? null
                : dataSource.getConnection();
        return result;
    }

    @Deprecated
    default FacetedQuery connection(SparqlQueryConnection connection) {
        return dataSource(connection == null ? null : RdfDataEngines.ofQueryConnection(connection));
    }


    /** Create a copy of this FacetedQuery where the base concept has been materialized into
     *  a SPARQL VALUES graph pattern.
     */

    // FacetedQuery materializeBaseConcept();

    /**
     * Lookup a facet node by id
     * @param id
     * @return
     */
    //FacetNode find(Object id);


    //void UnaryRelation getBaseConcept();
}
