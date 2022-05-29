package org.aksw.jenax.sparql.query.rx;

import java.util.List;

import org.aksw.jenax.stmt.core.SparqlStmtQuery;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import io.reactivex.rxjava3.core.Flowable;

public class ResultSetRxImpl
    implements ResultSetRx
{
	protected SparqlStmtQuery queryStmt;
    protected List<Var> vars;
    protected Flowable<Binding> bindings;

    public ResultSetRxImpl(SparqlStmtQuery queryStmt, List<Var> vars, Flowable<Binding> bindings) {
        super();
        this.queryStmt = queryStmt;
        this.vars = vars;
        this.bindings = bindings;
    }

    @Override
    public SparqlStmtQuery getQueryStmt() {
    	return queryStmt;
    }

    @Override
    public List<Var> getVars() {
        return vars;
    }

    @Override
    public Flowable<Binding> getBindings() {
        return bindings;
    }

    public static ResultSetRxImpl create(List<Var> vars, Flowable<Binding> bindings) {
        return new ResultSetRxImpl(null, vars, bindings);
    }

    public static ResultSetRxImpl create(Query query, List<Var> vars, Flowable<Binding> bindings) {
        return new ResultSetRxImpl(query == null ? null : new SparqlStmtQuery(query), vars, bindings);
    }
}
