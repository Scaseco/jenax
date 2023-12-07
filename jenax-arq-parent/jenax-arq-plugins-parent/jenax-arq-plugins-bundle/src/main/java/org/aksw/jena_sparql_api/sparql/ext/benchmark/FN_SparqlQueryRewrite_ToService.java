package org.aksw.jena_sparql_api.sparql.ext.benchmark;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;

import org.aksw.commons.io.util.UriUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;

/**
 * Given a SPARQL select query, rewrite it such that the original query is encoded in a service clause.
 * The main transformation is encode the original query's FROM (NAMED) clauses in the
 * default-graph-uri and named-graph-uri in the query string of the given service URI.
 */
public class FN_SparqlQueryRewrite_ToService
    extends FunctionBase2
{
    @Override
    public NodeValue exec(NodeValue serviceNv, NodeValue queryNv) {
        // Node serviceNode = serviceNv.asNode();
        // XXX Could validate term types
        String serviceUriStr = serviceNv.asNode().getLiteralLexicalForm();
        String queryStr = queryNv.asNode().getLiteralLexicalForm();

        Query baseQuery = QueryFactory.create(queryStr);
        DatasetDescription dd = baseQuery.getDatasetDescription();
        String finalUriStr = serviceUriStr;
        if (dd != null && !dd.isEmpty()) {
            try {
                URI uri = new URI(serviceUriStr);
                List<Entry<String, String>> args = UriUtils.parseQueryStringAsList(uri.getRawQuery());
                dd.getDefaultGraphURIs().forEach(item -> args.add(new SimpleEntry<>("default-graph-uri", item)));
                dd.getNamedGraphURIs().forEach(item -> args.add(new SimpleEntry<>("named-graph-uri", item)));
                String newQueryString = UriUtils.toQueryString(args);
                finalUriStr = UriUtils.replaceQueryString(uri, newQueryString).toString();
            } catch (URISyntaxException e) {
                // throw new ExprEvalException("");
                throw new RuntimeException(e);
            }
        }

        Query modifiedBaseQuery = QueryTransformOps.shallowCopy(baseQuery);
        modifiedBaseQuery.setQuerySelectType();
        modifiedBaseQuery.getPrefixMapping().clearNsPrefixMap();
        modifiedBaseQuery.getGraphURIs().clear();
        modifiedBaseQuery.getNamedGraphURIs().clear();
        modifiedBaseQuery.setLimit(Query.NOLIMIT);
        modifiedBaseQuery.setOffset(Query.NOLIMIT);

        Query newQuery = new Query();
        newQuery.setQuerySelectType();
        newQuery.setQueryResultStar(true);
        newQuery.setQueryPattern(new ElementService(finalUriStr, new ElementSubQuery(modifiedBaseQuery)));

        // XXX possibly create a more light-weight version of restoreQueryForm
        Query finalQuery = QueryUtils.restoreQueryForm(newQuery, baseQuery);
        finalQuery.getGraphURIs().clear();
        finalQuery.getNamedGraphURIs().clear();
        finalQuery.setLimit(baseQuery.getLimit());
        finalQuery.setOffset(baseQuery.getOffset());

        return NodeValue.makeString(finalQuery.toString());
    }
}
