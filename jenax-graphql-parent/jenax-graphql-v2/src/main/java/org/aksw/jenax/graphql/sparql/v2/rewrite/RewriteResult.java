package org.aksw.jenax.graphql.sparql.v2.rewrite;

import java.util.Map;

public class RewriteResult<K> {
    protected GraphQlFieldRewrite<K> root;
    protected Map<String, GraphQlFieldRewrite<K>> map;

    public GraphQlFieldRewrite<K> root() {
        return root;
    }

    public Map<String, GraphQlFieldRewrite<K>> map() {
        return map;
    }
}
