package org.aksw.jenax.dataaccess.sparql.polyfill.detector;

import org.aksw.jenax.stmt.core.SparqlStmtQuery;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.algebra.Table;

/**
 * A materialized query execution result.
 */
public class ExecResult {
    /** Using SparqlStmtQuery because it allows for non-parsed queries */
    protected SparqlStmtQuery query;
    protected Throwable throwable;

    /** The effective value of the exec result */
    protected Boolean value;

    /* */
    protected Table table;
    protected Model model;
    protected Dataset dataset;

    public ExecResult(SparqlStmtQuery query, Throwable throwable, Boolean value, Table table, Model model, Dataset dataset) {
        super();
        this.query = query;
        this.throwable = throwable;
        this.value = value;

        this.table = table;
        this.model = model;
        this.dataset = dataset;
    }

    public SparqlStmtQuery getQuery() {
        return query;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Table getTable() {
        return table;
    }

    public Model getModel() {
        return model;
    }

    public Boolean getValue() {
        return value;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public static ExecResult of(SparqlStmtQuery query, Throwable throwable, Boolean value) {
        return new ExecResult(query, throwable, value, null, null, null);
    }

    public static ExecResult of(SparqlStmtQuery query, Table table, Boolean value) {
        return new ExecResult(query, null, value, table, null, null);
    }

    public static ExecResult of(SparqlStmtQuery query, Model model, Boolean value) {
        return new ExecResult(query, null, value, null, model, null);
    }

    public static ExecResult of(SparqlStmtQuery query, Dataset dataset, Boolean value) {
        return new ExecResult(query, null, value, null, null, dataset);
    }
}
