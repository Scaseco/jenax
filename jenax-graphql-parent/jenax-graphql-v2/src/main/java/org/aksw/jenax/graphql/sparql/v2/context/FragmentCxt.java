package org.aksw.jenax.graphql.sparql.v2.context;

import java.util.List;

import org.apache.jena.sparql.core.Var;

import graphql.language.Node;

public record FragmentCxt(Node startNode,  List<Var> connectVars) {
}
