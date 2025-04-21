package org.aksw.jenax.graphql.sparql.v2.context;

import java.util.Objects;

public class CardinalityDirective extends Cascadable {
    protected boolean isOne;

    public CardinalityDirective(boolean isOne, Cascadable cascadable) {
        this(isOne, cascadable.isSelf(), cascadable.isCascade());
    }

    public CardinalityDirective(boolean isOne, boolean isSelf, boolean isCascade) {
        super(isSelf, isCascade);
        this.isOne = isOne;
    }

    public boolean isOne() {
        return isOne;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(isOne);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        CardinalityDirective other = (CardinalityDirective) obj;
        return isOne == other.isOne;
    }

    @Override
    public String toString() {
        return "CardinalityDirective [isOne=" + isOne + ", toString()=" + super.toString() + "]";
    }
}
