package org.aksw.jena_sparql_api.lookup;

import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecFactoryQuery;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParser;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParserImpl;
import org.apache.jena.query.Query;

public class SparqlFlowEngine {
    //protected Function<String, Query> queryParser;
    protected SparqlQueryParser queryParser;
    protected QueryExecFactoryQuery qef;

    public SparqlFlowEngine(QueryExecFactoryQuery qef) {
        this.qef = qef;
        queryParser = SparqlQueryParserImpl.create();
    }

    public PaginatorQueryBinding fromSelect(String queryStr) {
        Query query = queryParser.apply(queryStr);
        PaginatorQueryBinding result = fromSelect(query);
        return result;
    }

    public PaginatorQueryBinding fromSelect(Query query) {
        PaginatorQueryBinding result = new PaginatorQueryBinding(qef, query);
        return result;
    }

    public PaginatorQueryTriple fromConstruct(String queryStr) {
        Query query = queryParser.apply(queryStr);
        PaginatorQueryTriple result = fromConstruct(query);
        return result;
    }

    public PaginatorQueryTriple fromConstruct(Query query) {
        PaginatorQueryTriple result = new PaginatorQueryTriple(qef, query);
        return result;
    }
}
