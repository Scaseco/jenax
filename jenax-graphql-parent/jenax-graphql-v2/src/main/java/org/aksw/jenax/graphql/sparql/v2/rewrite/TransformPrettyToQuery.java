package org.aksw.jenax.graphql.sparql.v2.rewrite;

/**
 * Adds <pre>@pretty</pre> to a query operation if any of its direct children has that directive.
 * If the directive is present on the query then the json output will be pretty printed.
 */
public class TransformPrettyToQuery
    extends TransformDirectiveOnTopLevelFieldToQueryBase
{
    public TransformPrettyToQuery() {
        super("pretty");
    }
}
