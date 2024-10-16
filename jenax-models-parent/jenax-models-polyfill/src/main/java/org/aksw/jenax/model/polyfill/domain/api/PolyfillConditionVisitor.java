package org.aksw.jenax.model.polyfill.domain.api;

public interface PolyfillConditionVisitor<T> {
    public T visit(PolyfillConditionQuery condition);
    public T visit(PolyfillConditionConjunction condition);
}
