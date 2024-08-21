package org.aksw.jenax.graphql.sparql.v2.model;

import java.util.List;

import org.aksw.jenax.graphql.sparql.v2.rewrite.RewriteResult.SingleResult;
import org.apache.jena.sparql.core.Var;


// This class is essentially a rewrite result with a list of default variables to connect on
public record ElementNodeFragment<K>(SingleResult<K> rewrite, List<Var> defaultConnectVars) {
    public ElementNodeFragment {
        // XXX Validate that the connect variables are visible in elementNode
    }
}
