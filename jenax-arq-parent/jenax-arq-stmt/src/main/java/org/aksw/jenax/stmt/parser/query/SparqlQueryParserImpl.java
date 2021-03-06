package org.aksw.jenax.stmt.parser.query;

import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.stmt.core.SparqlParserConfig;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Prologue;

public class SparqlQueryParserImpl
    implements SparqlQueryParser
{
    protected Supplier<Query> querySupplier;
    protected Syntax syntax;
    protected String baseURI;

    public SparqlQueryParserImpl() {
        this(new QuerySupplierImpl(), Syntax.syntaxARQ, "http://www.example.org/base/");
    }

    public SparqlQueryParserImpl(Supplier<Query> querySupplier, Syntax syntax, String baseURI) {
        super();
        this.querySupplier = querySupplier;
        this.syntax = syntax;
        this.baseURI = baseURI;
    }

    @Override
    public Query apply(String queryString) {
        Query result = querySupplier.get();
        QueryFactory.parse(result, queryString, baseURI, syntax);

        return result;
    }

    public static SparqlQueryParserImpl create(SparqlParserConfig config) {
        SparqlQueryParserImpl result = create(config.getSyntax(), config.getPrologue(), config.getBaseURI(), config.getSharedPrefixes());
        return result;
    }

    public static SparqlQueryParserImpl create() {
        SparqlQueryParserImpl result = create(Syntax.syntaxARQ, null);
        return result;
    }

    public static SparqlQueryParserImpl create(PrefixMapping prefixMapping) {
        SparqlQueryParserImpl result = create(Syntax.syntaxARQ, new Prologue(prefixMapping));
        return result;
    }

    public static SparqlQueryParserImpl create(Syntax syntax) {
        SparqlQueryParserImpl result = create(syntax, null);
        return result;
    }

    public static SparqlQueryParserImpl create(Syntax syntax, Prologue prologue) {
        SparqlQueryParserImpl result = create(syntax, prologue, "", null);
        return result;
    }

    public static SparqlQueryParserImpl create(Syntax syntax, Prologue prologue, String baseURI, PrefixMapping sharedPrefixes) {
        Supplier<Query> querySupplier = new QuerySupplierImpl(prologue, baseURI, sharedPrefixes);

        SparqlQueryParserImpl result = new SparqlQueryParserImpl(querySupplier, syntax, null);
        return result;
    }


    /** Create a parser that leaves relative IRIs untouched */
    public static SparqlQueryParserImpl createAsGiven() {
        return create(SparqlParserConfig.newInstance().parseAsGiven().applyDefaults());
    }

    // TODO Move to common query parser utils
    public static SparqlQueryParser wrapWithOptimizePrefixes(Function<String, Query> delegate) {
        return str -> {
            Query r = delegate.apply(str);
            QueryUtils.optimizePrefixes(r);
            return r;
        };
    }
}
