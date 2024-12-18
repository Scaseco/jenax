package org.aksw.jenax.graphql.sparql.v2.rewrite;

/**
 * Adds <pre>@debug</pre> to a query operation if any of its direct children has that directive.
 */
public class TransformDebugToQuery
    extends TransformDirectiveOnTopLevelFieldToQueryBase
{
    public TransformDebugToQuery() {
        super("debug");
    }
}
