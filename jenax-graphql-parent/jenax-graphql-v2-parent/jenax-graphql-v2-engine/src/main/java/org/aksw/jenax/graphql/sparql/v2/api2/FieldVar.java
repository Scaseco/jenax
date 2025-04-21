package org.aksw.jenax.graphql.sparql.v2.api2;

import org.aksw.jenax.graphql.sparql.v2.model.ElementNode;
import org.apache.jena.sparql.core.Var;

/** Reference to a variable in a field. */
public record FieldVar(ElementNode field, Var var) {}
