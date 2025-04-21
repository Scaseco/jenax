package org.aksw.jenax.graphql.sparql.v2.context;

import java.util.Objects;

public class Cascadable {
    protected boolean isSelf;
    protected boolean isCascade;

    public Cascadable(boolean isSelf, boolean isCascade) {
        super();
        this.isSelf = isSelf;
        this.isCascade = isCascade;
    }

    public boolean isSelf() {
        return isSelf;
    }

    public boolean isCascade() {
        return isCascade;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isCascade, isSelf);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Cascadable other = (Cascadable) obj;
        return isCascade == other.isCascade && isSelf == other.isSelf;
    }

    @Override
    public String toString() {
        return "Cascadable [isSelf=" + isSelf + ", isCascade=" + isCascade + "]";
    }
}
