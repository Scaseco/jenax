package org.aksw.jenax.sparql.query.rx;

import java.util.Iterator;
import java.util.List;

import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecOverRowSet;
import org.aksw.jenax.sparql.rx.op.FlowOfBindingsOps;
import org.aksw.jenax.stmt.core.SparqlStmtQuery;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetStream;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * A result set based on rx Flowables.
 * Essentially a pair of variable (names) and a flowable of bindings.
 * Provides {@link #asQueryExec()} and {@link #asQueryExecution()} which return fresh objects that
 * support select-based execution with abort and close.
 *
 */
public interface ResultSetRx {
    // Reference to the query in order to be able to supply it
    // in the wrappers returned by the asQueryExec* methods
    SparqlStmtQuery getQueryStmt();
    List<Var> getVars();
    Flowable<Binding> getBindings();

    default QueryIterator asQueryIterator() {
        Flowable<Binding> bindingFlow = getBindings();
        return FlowOfBindingsOps.toQueryIterator(bindingFlow);
    }

    /**
     * Returns a QueryExecution with only support for execSelect, abort and close
     *
     * @return A query execution wrapping this result set
     */
    default QueryExecution asQueryExecution() {
        QueryExec qExec = asQueryExec();
        return org.apache.jena.sparql.exec.QueryExecutionAdapter.adapt(qExec);
    }

    default QueryExec asQueryExec() {
        SparqlStmtQuery queryStmt = getQueryStmt();
        Query query = queryStmt == null ? null : queryStmt.getQuery();
        QueryExec result = new QueryExecOverRowSet(query) {
            protected Disposable disposable = null;

//            @Override
//            public Query getQuery() {
//            	SparqlStmtQuery stmt = getQueryStmt();
//            	Query result = stmt != null && stmt.isParsed() ? stmt.getQuery() : null;
//            	return result;
//            }
//
//            @Override
//            public String getQueryString() {
//            	SparqlStmtQuery stmt = getQueryStmt();
//            	String result = stmt == null ? null : stmt.getOriginalString();
//            	return result;
//            }
//
            @Override
            public void abort() {
                close();
            }

            @Override
            public void closeActual() {
                if (disposable != null) {
                    disposable.dispose();
                }
                // super.closeActual();
            }

            @Override
            protected RowSet createRowSet(Query query) {
                if (disposable != null) {
                    throw new IllegalStateException("execSelect has already been called");
                }

                List<Var> vars = getVars();
                Flowable<Binding> flowable = getBindings();
                Iterator<Binding> it = flowable.blockingIterable().iterator();
                disposable = (Disposable)it;
                RowSet result = RowSetStream.create(vars, it);
                return result;
            }
        };

        return result;
    }

//    default QueryExecution asQueryExecution(Template template) {
//
//        QueryExecution result = new QueryExecutionAdapter() {
//            protected Disposable disposable = null;
//
//            @Override
//            public Iterator<Quad> execConstructQuads() {
//                if (template == null) {
//                    throw new IllegalStateException("No template set");
//                }
//
//                return null;
//                //TemplateLib.subst(quad, b, bNodeMap)
//            }
//
//            @Override
//            public ResultSet execSelect() {
//                if (disposable != null) {
//                    throw new IllegalStateException("execSelect has already been called");
//                }
//
//                List<Var> vars = getVars();
//                Flowable<Binding> flowable = getBindings();
//                Iterator<Binding> it = flowable.blockingIterable().iterator();
//                disposable = (Disposable)it;
//                ResultSet result = ResultSetUtils.createUsingVars(vars, it);
//                return result;
//            }
//
//            @Override
//            public void abort() {
//                super.abort();
//                close();
//            }
//
//            @Override
//            public void close() {
//                if (disposable != null) {
//                    disposable.dispose();
//                }
//                super.close();
//            }
//        };
//
//        return result;
//    }
}
