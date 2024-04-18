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

    public static final String sparql = "sparql";
    public static final String fragment = "fragment";
    public static final String inject = "inject";

    public static final String one = "one";
    public static final String many = "many";
    public static final String cascade = "cascade";
    public static final String self = "self";

}
