package org.aksw.jenax.graphql.sparql.v2.rewrite;

/**
 * Adds <pre>@ordered</pre> to a query operation if any of its direct children has that directive.
 * If the directive is present on the query then the SPARQL query rewrite will include a global ORDER BY clause.
 * This is needed when the target SPARQL engine does not preserve order of bindings
 * especially for union, lateral and extend operations.
 * This flag should not be used in production, instead the rewrite mode should be set on the graphql engine level.
 * The flag exists to make it easy to try out the ordered rewrite.
 */
public class TransformPullOrdered
    extends TransformDirectiveOnTopLevelFieldToQueryBase
{
    public TransformPullOrdered() {
        super("ordered");
    }
}
