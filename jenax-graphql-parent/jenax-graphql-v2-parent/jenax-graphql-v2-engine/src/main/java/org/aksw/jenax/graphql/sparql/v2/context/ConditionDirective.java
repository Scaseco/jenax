package org.aksw.jenax.graphql.sparql.v2.context;

import java.util.List;

/** Expression by which to filter the parent resources. */
public record ConditionDirective(String exprStr, List<String> thisVars, List<String> parentVars) {}
