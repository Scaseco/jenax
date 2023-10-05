package org.aksw.jenax.dataaccess.sparql.exec.query;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.aksw.jenax.dataaccess.sparql.execution.query.QueryExecutionSupplier;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecAdapter;

@FunctionalInterface
public interface QueryExecSupplier {

    QueryExec get();

    public static QueryExecSupplier adapt(QueryExecutionSupplier upper) {
        return () -> QueryExecAdapter.adapt(upper.get());
    }

    public static QueryExecSupplier of(Supplier<QueryExec> supp) {
        return supp::get;
    }

    public static QueryExecSupplier of(Callable<QueryExec> supp) {
        return () -> {
            QueryExec r;
            try {
                r = supp.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return r;
        };
    }
}
