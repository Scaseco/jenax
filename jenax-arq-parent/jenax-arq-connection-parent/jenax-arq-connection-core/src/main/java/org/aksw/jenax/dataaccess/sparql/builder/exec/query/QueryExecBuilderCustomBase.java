package org.aksw.jenax.dataaccess.sparql.builder.exec.query;

import java.util.concurrent.TimeUnit;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

public abstract class QueryExecBuilderCustomBase<T extends QueryExecBuilder>
    extends QueryExecModCustomBase<T>
    implements QueryExecBuilder
{
    protected Query query;
    protected String queryString;
    protected Syntax querySyntax;
    protected BindingBuilder substitution;

    public QueryExecBuilderCustomBase(Context context) {
        super(context);
    }

    public Query getParsedQuery() {
        Query result = query != null
                ? query
                : QueryFactory.create(queryString, querySyntax);
        return result;
    }

    @Override
    public QueryExecBuilder query(Query query) {
        this.querySyntax = null;
        this.queryString = null;
        this.query = query;
        return self();
    }

    @Override
    public QueryExecBuilder query(String queryString) {
        this.query = null;
        this.querySyntax = null;
        this.queryString = queryString;
        return self();
    }

    @Override
    public QueryExecBuilder query(String queryString, Syntax syntax) {
        this.query = null;
        this.queryString = queryString;
        this.querySyntax = syntax;
        return self();
    }

    @Override
    public QueryExecBuilder set(Symbol symbol, Object value) {
        getContext().set(symbol, value);
        return self();
    }

    @Override
    public QueryExecBuilder set(Symbol symbol, boolean value) {
        getContext().set(symbol, value);
        return self();
    }

    @Override
    public QueryExecBuilder context(Context context) {
        Context.mergeCopy(getContext(), context);
        return self();
    }

    @Override
    public QueryExecBuilder substitution(Binding binding) {
        substitution.addAll(binding);
        return self();
    }

    @Override
    public QueryExecBuilder substitution(Var var, Node value) {
        substitution.add(var, value);
        return self();
    }

    @Override
    public QueryExecBuilder timeout(long value, TimeUnit timeUnit) {
        overallTimeout(value, timeUnit);
        return self();
    }

    public void applySettings(QueryExecBuilder dst) {
        super.applySettings(dst);

        if (querySyntax != null) {
            dst.query(queryString, querySyntax);
        } else if (queryString != null) {
            dst.query(queryString);
        } else if (query != null) {
            dst.query(query);
        }

        Binding binding = substitution.build();
        if (!binding.isEmpty()) {
            dst.substitution(binding);
        }
    }
}
