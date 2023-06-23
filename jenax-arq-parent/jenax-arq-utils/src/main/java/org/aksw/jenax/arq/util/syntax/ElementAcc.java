package org.aksw.jenax.arq.util.syntax;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;

/**
 * Accumulator for elements. Elements are added to an ElementGroup that acts as a container
 * whereas the resulting Element may be a different element, such as an ElementOptional.
 */
public class ElementAcc {
    protected Var rootVar; // The root variable of the element to which any child elements connect
    protected Element resultElement;
    protected ElementGroup container;

    public ElementAcc(Var rootVar, Element rootElement, ElementGroup container) {
        super();
        this.rootVar = rootVar;
        this.resultElement = rootElement;
        this.container = container;
    }

    public Element getResultElement() {
        return resultElement;
    }

    public ElementGroup getContainer() {
        return container;
    }

    @Override
    public String toString() {
        return "ElementAcc [rootVar=" + rootVar + ", resultElement=" + resultElement + "]";
    }
}
