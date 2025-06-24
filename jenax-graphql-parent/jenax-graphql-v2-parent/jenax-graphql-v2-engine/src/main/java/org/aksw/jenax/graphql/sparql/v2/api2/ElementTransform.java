package org.aksw.jenax.graphql.sparql.v2.api2;

import java.util.Set;
import java.util.function.Function;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

public interface ElementTransform
    extends Function<Element, Element>
{
    Set<Var> getDeclaredVariables();
}
