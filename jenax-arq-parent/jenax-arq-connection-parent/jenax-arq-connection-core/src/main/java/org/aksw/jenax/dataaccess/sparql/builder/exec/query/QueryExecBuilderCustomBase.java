package org.aksw.jenax.dataaccess.sparql.builder.exec.query;

import java.util.concurrent.TimeUnit;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/**
 * Base class for custom QueryExecBuilders.
 * Tracks all configurations in protected attributes which can be accessed by subclasses.
 *
 * @param <T> The QueryExecBuilder type for fluent chains (typically {@link QueryExecBuilder}, but may be a custom sub-class).
 */
public abstract class QueryExecBuilderCustomBase<T extends QueryExecBuilder>
    extends QueryExecModCustomBase<T>
    implements QueryExecBuilder
{
    protected Query query;
    protected String queryString;
    protected Syntax querySyntax;
    protected Boolean parseCheck = null;
    protected BindingBuilder substitution = BindingFactory.builder();

    public QueryExecBuilderCustomBase() {
        super();
    }

    /** Copy constructor.
     *  Does not clone the query object. */
    public QueryExecBuilderCustomBase(QueryExecBuilderCustomBase<?> that) {
        super(that);
        this.query = that.query; // that.query.cloneQuery();
        this.queryString = that.queryString;
        this.querySyntax = that.querySyntax;
        this.parseCheck = that.parseCheck;
        this.substitution = Binding.builder().addAll(that.substitution.build());
    }

    public Query getQuery() {
        return query;
    }

    public String getQueryString() {
        return queryString;
    }

    public Syntax getQuerySyntax() {
        return querySyntax;
    }

    public BindingBuilder getSubstitution() {
        return substitution;
    }

    /**
     * Attempt to create a query object from this builder's state.
     * Each invocation parses the query anew.
     */
    public Query getParsedQuery() {
        Query result = query != null
                ? query
                : queryString != null
                    ? QueryFactory.create(queryString, querySyntax)
                    : null;
        return result;
    }

    /** Parse the query string or return a copy of the query object. */
    public Query getParsedQueryCopy() {
        Query result = query != null
                ? query.cloneQuery()
                : queryString != null
                    ? QueryFactory.create(queryString, querySyntax)
                    : null;
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
        this.query = null; // parseCheck ? QueryFactory.create(queryString) : null;;
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
    public QueryExecBuilder parseCheck(boolean parseCheck) {
        this.parseCheck = parseCheck;
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
        if (context != null) {
            for (Symbol key : context.keys()) {
                this.contextAccumulator.set(key, context.get(key));
            }
        }
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

    public <X extends QueryExecBuilder> X applySettings(X dst) {
        super.applySettings(dst);

        if (parseCheck != null) {
            dst.parseCheck(parseCheck);
        }

        if (querySyntax != null) {
            dst.query(queryString, querySyntax);
        } else if (queryString != null) {
            dst.query(queryString);
        } else if (query != null) {
            dst.query(query);
        }

        Binding binding = substitution.build();

        // Calling build() invalidates the builder - so we have to reset it.
        substitution.reset().addAll(binding);

        if (!binding.isEmpty()) {
            dst.substitution(binding);
        }

        return dst;
    }
}
