package org.aksw.facete.v3.impl;

import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionBuilder;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.SparqlQueryConnection;


public class FacetedQueryBuilder<P> {
    protected P parent;
    protected RDFConnectionBuilder<SparqlQueryConnection, FacetedQueryBuilder<P>> dataConnectionBuilder = new RDFConnectionBuilder<>(this);
    protected ResourceBuilder<FacetedQueryBuilder<P>> resourceBuilder = new ResourceBuilder<>(this);

    public RDFConnectionBuilder<SparqlQueryConnection, FacetedQueryBuilder<P>> configDataConnection() {
        return dataConnectionBuilder;
    }

    /**
     * Configure model and resource which will hold the state of the faceted query
     * @return
     */
    public ResourceBuilder<FacetedQueryBuilder<P>> configModel() {
        return resourceBuilder;
    }

    public FacetedQuery create() {
        SparqlQueryConnection conn = dataConnectionBuilder.getConnection();
        Resource fqModelRoot = resourceBuilder.getResource();

        if(fqModelRoot == null) {
            fqModelRoot = ModelFactory.createDefaultModel().createResource();
        }

        FacetedQuery result = FacetedQueryImpl.create(fqModelRoot, conn);
        return result;
    }

    public P end() {
        return parent;
    }

    public static FacetedQueryBuilder<?> builder() {
        return new FacetedQueryBuilder<Object>();
    }
}

