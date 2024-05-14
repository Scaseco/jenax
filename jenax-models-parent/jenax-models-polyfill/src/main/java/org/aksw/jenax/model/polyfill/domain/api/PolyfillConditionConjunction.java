package org.aksw.jenax.model.polyfill.domain.api;

import java.util.List;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.Namespace;
import org.aksw.jenax.annotation.reprogen.RdfType;

@Namespace("https://w3id.org/aksw/norse#polyfill.")
@RdfType
public interface PolyfillConditionConjunction
    extends PolyfillCondition
{
    @IriNs
    List<PolyfillCondition> getConditions();

    default PolyfillConditionConjunction addCondition(PolyfillCondition condition) {
        getConditions().add(condition);
        return this;
    }

    @Override
    default <T> T accept(PolyfillConditionVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
