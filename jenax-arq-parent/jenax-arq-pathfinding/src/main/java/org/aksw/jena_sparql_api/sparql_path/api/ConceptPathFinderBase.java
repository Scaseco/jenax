package org.aksw.jena_sparql_api.sparql_path.api;

import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.apache.jena.graph.Graph;

public abstract class ConceptPathFinderBase
    implements ConceptPathFinder
{
    protected Graph dataSummary;
    protected RDFDataSource dataSource;

    public ConceptPathFinderBase(Graph dataSummary, RDFDataSource dataSource) {
        super();
        this.dataSummary = dataSummary;
        this.dataSource = dataSource;
    }
}
