package org.aksw.jenax.graphql.sparql.v2.rewrite;

/**
 * Adds <pre>@ndJson</pre> to a query operation if any of its direct children has that directive.
 * Presence of this directive alters the output from json to newline delimited json.
 */
public class TransformPullNdJson
    extends TransformDirectiveOnTopLevelFieldToQueryBase
{
    public TransformPullNdJson() {
        super("ndJson");
    }
}
