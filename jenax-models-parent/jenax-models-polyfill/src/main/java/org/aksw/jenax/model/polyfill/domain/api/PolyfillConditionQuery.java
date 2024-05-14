package org.aksw.jenax.model.polyfill.domain.api;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.Namespace;
import org.aksw.jenax.annotation.reprogen.RdfType;

@Namespace("https://w3id.org/aksw/norse#polyfill.")
@RdfType
public interface PolyfillConditionQuery
    extends PolyfillCondition
{
    @IriNs
    String getQueryString();
    PolyfillConditionQuery setQueryString(String queryString);

    /** Explicitly declare query strings that for some reason cannot be parsed by Jena's ARQ parser */
    @IriNs
    Boolean isNonParseable();
    PolyfillConditionQuery setNonParseable(Boolean value);

    /**
     * Polyfill conditions are by default satisfied if the query fails.
     * If matchOnResult is true, then a condition is satisfied if the query returns a non-empty result.
     * This can be used to check for the presence of vendor/version specific features.
     */
    @IriNs
    Boolean isMatchOnNonEmptyResult();
    PolyfillConditionQuery setMatchOnNonEmptyResult(Boolean value);

    @Override
    default <T> T accept(PolyfillConditionVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
