package org.aksw.jenax.graphql.sparql;

public class GraphQlSpecialKeys {
    /** "@id" with '@' replaced by 'x' because '@' is not a valid in graphql identifiers */
    public static final String xid = "xid";

    /** Argument name to order by paths of fields*/
    public static final String orderBy = "orderBy";

    /**
     * Directive name to 'hide' intermediate nodes / json documents
     * Flattens children of x into the parent of x
     */
    public static final String hide = "hide"; // Attach all sub-fields of "this node" to the parent

    /** Directive name for inverse properties */
    public static final String inverse = "inverse";
}