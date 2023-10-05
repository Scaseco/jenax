package org.aksw.facete.v3.impl;

import java.util.List;

import org.aksw.facete.v3.api.FacetedDataQuery;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.data_query.impl.DataQueryImpl;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.Template;


/**
 * An extension of {@link DataQueryImpl} that allows obtaining {@link FacetedQuery} instances for
 * node views.
 *
 * This allows filtering the set of values using faceted search.
 * Especially, it makes it easy to retriev the available properties for that variable's values, which
 * can be used as suggestions for adding new columns to a table view.
 *
 * @author raven
 *
 * @param <T>
 */
public class FacetedDataQueryImpl<T extends RDFNode>
    extends DataQueryImpl<T>
    implements FacetedDataQuery<T>
{
    public FacetedDataQueryImpl(RdfDataSource dataSource, Element baseQueryPattern, Var rootVar, Template template,
            Class<T> resultClass) {
        super(dataSource, baseQueryPattern, rootVar, template, resultClass);
    }

    @Deprecated
    public FacetedDataQueryImpl(RdfDataSource dataSource, UnaryRelation baseRelation, Template template,
            Class<T> resultClass) {
        super(dataSource, baseRelation, template, resultClass);
    }

    @Deprecated
    public FacetedDataQueryImpl(RdfDataSource dataSource, Element baseElement, List<Var> primaryKeyVars,
            Node superRootNode, Var defaultVar, Template template, Class<T> resultClass) {
        super(dataSource, baseElement, primaryKeyVars, superRootNode, defaultVar, template, resultClass);
    }

    @Deprecated
    public FacetedDataQueryImpl(SparqlQueryConnection conn, Element baseQueryPattern, Var rootVar, Template template,
            Class<T> resultClass) {
        super(conn, baseQueryPattern, rootVar, template, resultClass);
    }

    @Deprecated
    public FacetedDataQueryImpl(SparqlQueryConnection conn, UnaryRelation baseRelation, Template template,
            Class<T> resultClass) {
        super(conn, baseRelation, template, resultClass);
    }

    @Deprecated
    public FacetedDataQueryImpl(SparqlQueryConnection conn, Element baseElement, List<Var> primaryKeyVars,
            Node superRootNode, Var defaultVar, Template template, Class<T> resultClass) {
        super(conn, baseElement, primaryKeyVars, superRootNode, defaultVar, template, resultClass);
    }

    @Override
    public FacetedQuery toFacetedQuery() {
        FacetedQuery fq = FacetedQueryBuilder.builder()
            .configDataConnection().setSource(connection()).end()
            .create();

        Element el = this.baseElement();
        Var var = this.getDefaultVar();

        UnaryRelation rel = new Concept(el, var);

        fq.baseConcept(rel);

        return fq;
    }
}
