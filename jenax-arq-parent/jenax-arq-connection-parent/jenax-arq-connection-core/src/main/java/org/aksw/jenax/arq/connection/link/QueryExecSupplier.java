package org.aksw.jenax.arq.connection.link;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.aksw.jenax.connection.query.QueryExecutionSupplier;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecAdapterFix;

@FunctionalInterface
public interface QueryExecSupplier {

	QueryExec get();

	public static QueryExecSupplier adapt(QueryExecutionSupplier upper) {
		return () -> QueryExecAdapterFix.adapt(upper.get());
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
