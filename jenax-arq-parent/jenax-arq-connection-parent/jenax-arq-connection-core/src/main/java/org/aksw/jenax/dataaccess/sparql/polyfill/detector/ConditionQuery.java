package org.aksw.jenax.dataaccess.sparql.polyfill.detector;

import java.util.Objects;

import org.aksw.commons.util.exception.ExceptionUtilsAksw;
import org.aksw.jenax.arq.util.binding.TableUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.stmt.core.SparqlStmtQuery;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.algebra.Table;

public class ConditionQuery
    implements Condition
{
    protected SparqlStmtQuery query;
    protected boolean matchOnNonEmptyResult;

    public ConditionQuery(SparqlStmtQuery query, boolean matchOnNonEmptyResult) {
        super();
        this.query = Objects.requireNonNull(query);
        this.matchOnNonEmptyResult = matchOnNonEmptyResult;

        // We need the query type to determine the appropriate response type
        if (!this.query.isParsed()) {
            throw new RuntimeException("Non-parsed queries not supported yet");
        }
    }

    @Override
    public boolean test(RdfDataSource dataSource) {
        boolean result;
        try (RDFConnection conn = dataSource.getConnection()) {
            try (QueryExecution qe = conn.query(query.getQuery())) {
                ResultSet rs = qe.execSelect();
                Table table = TableUtils.createTable(rs);
                result = matchOnNonEmptyResult ? !table.isEmpty() : false;
            }
        } catch (Exception e) {
            boolean isConnectionProblem = isConnectionProblemException(e);
            if (isConnectionProblem) {
                // Forward the exception
                e.addSuppressed(new RuntimeException("Connection problem detected", e));
                throw e;
            } else {
                result = matchOnNonEmptyResult ? false : true;
            }
        }
        return result;
    }

    // XXX Consolidate with SparqlQueryConnectionWithReconnect
    protected boolean isConnectionProblemException(Throwable t) {
        return ExceptionUtilsAksw.isConnectionRefusedException(t)
                || ExceptionUtilsAksw.isUnknownHostException(t);
    }

}
