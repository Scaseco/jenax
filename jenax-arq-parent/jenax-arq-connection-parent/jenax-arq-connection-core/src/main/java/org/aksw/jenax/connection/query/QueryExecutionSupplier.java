package org.aksw.jenax.connection.query;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.aksw.jenax.arq.connection.link.QueryExecSupplier;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.exec.QueryExecutionAdapter;

@FunctionalInterface
public interface QueryExecutionSupplier {
	QueryExecution get();

	default QueryExecSupplier levelDown() {
		return QueryExecSupplier.adapt(this);
	}

	public static QueryExecutionSupplier adapt(QueryExecSupplier lower) {
		return () -> QueryExecutionAdapter.adapt(lower.get());
	}

	public static QueryExecutionSupplier of(Supplier<QueryExecution> supp) {
		return supp::get;
	}

	public static QueryExecutionSupplier of(Callable<QueryExecution> supp) {
		return () -> {
			QueryExecution r;
			try {
				r = supp.call();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return r;
		};
	}
}
