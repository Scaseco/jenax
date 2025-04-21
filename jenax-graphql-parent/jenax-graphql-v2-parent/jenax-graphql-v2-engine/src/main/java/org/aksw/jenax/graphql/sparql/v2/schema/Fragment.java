package org.aksw.jenax.graphql.sparql.v2.schema;

import java.util.List;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

public record Fragment(Element element, List<Var> vars) {
}
