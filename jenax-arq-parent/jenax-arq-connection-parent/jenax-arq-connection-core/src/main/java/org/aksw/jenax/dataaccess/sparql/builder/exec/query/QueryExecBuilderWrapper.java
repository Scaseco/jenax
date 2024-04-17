package org.aksw.jenax.dataaccess.sparql.builder.exec.query;

import java.util.concurrent.TimeUnit;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

public interface QueryExecBuilderWrapper<T extends QueryExecBuilder>
    extends QueryExecBuilder, QueryExecModWrapper<T>
{
    @Override
    QueryExecBuilder getDelegate();

    @Override
    default T query(Query query) {
        getDelegate().query(query);
        return self();
    }

    /** Set the query. */
    @Override
    default T query(String queryString) {
        getDelegate().query(queryString);
        return self();
    }

    /** Set the query. */
    @Override
    default T query(String queryString, Syntax syntax) {
        getDelegate().query(queryString, syntax);
        return self();
    }

    /** Set a context entry. */
    @Override
    default T set(Symbol symbol, Object value) {
        getDelegate().set(symbol, value);
        return self();
    }

    /** Set a context entry. */
    @Override
    default T set(Symbol symbol, boolean value) {
        getDelegate().set(symbol, value);
        return self();
    }

    /**
     * Set the context. If not set, defaults to the system context
     * ({@link ARQ#getContext}).
     */
    @Override
    default T context(Context context) {
        getDelegate().context(context);
        return self();
    }

    /** Provide a set of (Var, Node) for substitution in the query when QueryExec is built. */
    @Override
    default T substitution(Binding binding) {
        getDelegate().substitution(binding);
        return self();
    }

    /** Provide a (Var, Node) for substitution in the query when QueryExec is built. */
    @Override
    default T substitution(Var var, Node value) {
        getDelegate().substitution(var, value);
        return self();
    }

    /** Provide a (var name, Node) for substitution in the query when QueryExec is built. */
    @Override
    public default T substitution(String var, Node value) {
        getDelegate().substitution(var, value);
        return self();
    }

    /** Set the overall query execution timeout. */
    @Override
    default T timeout(long value, TimeUnit timeUnit) {
        getDelegate().timeout(value, timeUnit);
        return self();
    }

    /**
     * Build the {@link QueryExec}. Further changes to he builder do not affect this
     * {@link QueryExec}.
     */
    @Override
    default QueryExec build() {
        return getDelegate().build();
    }

    // build-and-use short cuts

    /** Build and execute as a SELECT query. */
    @Override
    public default RowSet select() {
        return build().select();
    }

    /** Build and execute as a CONSTRUCT query. */
    @Override
    public default Graph construct() {
        try ( QueryExec qExec = build() ) {
            return qExec.construct();
        }
    }

    /** Build and execute as a CONSTRUCT query. */
    @Override
    public default Graph describe() {
        try ( QueryExec qExec = build() ) {
            return qExec.describe();
        }
    }

    /** Build and execute as an ASK query. */
    @Override
    public default boolean ask() {
        try ( QueryExec qExec = build() ) {
            return qExec.ask();
        }
    }
}
