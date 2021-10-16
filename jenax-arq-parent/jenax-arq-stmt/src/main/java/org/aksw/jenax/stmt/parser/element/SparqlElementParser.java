package org.aksw.jenax.stmt.parser.element;

import java.util.function.Function;

import org.apache.jena.sparql.syntax.Element;



public interface SparqlElementParser
    extends Function<String, Element>
{

}
