package org.aksw.jena_sparql_api.concept.builder.impl;

import org.aksw.jena_sparql_api.concept.builder.api.NodeBuilder;
import org.aksw.jenax.arq.util.var.Vars;

public class NodeBuilderFluent {
    public static NodeBuilder start() {
        NodeBuilder result = new NodeBuilderImpl(Vars.s);
        return result;
    }
}
