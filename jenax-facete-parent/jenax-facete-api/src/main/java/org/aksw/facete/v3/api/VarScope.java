package org.aksw.facete.v3.api;

import java.util.Objects;

import org.apache.jena.sparql.core.Var;

/**
 * A qualified variable.
 */
public class VarScope {
    protected String scopeName;
    protected Var startVar;

    protected VarScope(String scopeName, Var startVar) {
        super();
        this.scopeName = scopeName;
        this.startVar = startVar;
    }

    public static VarScope of(Var rootVar) {
        return of("", rootVar);
    }

    public static VarScope of(String scopeName, Var rootVar) {
        return new VarScope(scopeName, rootVar);
    }

    public String getScopeName() {
        return scopeName;
    }

    public Var getStartVar() {
        return startVar;
    }

    @Override
    public int hashCode() {
        return Objects.hash(scopeName, startVar);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VarScope other = (VarScope) obj;
        if (scopeName == null) {
            if (other.scopeName != null)
                return false;
        } else if (!scopeName.equals(other.scopeName))
            return false;
        if (startVar == null) {
            if (other.startVar != null)
                return false;
        } else if (!startVar.equals(other.startVar))
            return false;
        return true;
    }

    public static String toString(String str) {
        return str == null ? "(null)" : str.isEmpty() ? "(empty)" : str.isBlank() ? "blank" : str;
    }
    @Override
    public String toString() {
        return "VarScope(" + toString(scopeName) + ", " + startVar + ")";
    }
}
