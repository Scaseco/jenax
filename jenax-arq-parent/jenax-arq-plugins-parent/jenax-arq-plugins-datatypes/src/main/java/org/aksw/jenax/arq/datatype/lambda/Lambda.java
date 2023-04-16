package org.aksw.jenax.arq.datatype.lambda;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

public class Lambda {
    protected List<Var> params;
    protected Expr expr;

    public Lambda(List<Var> params, Expr expr) {
        super();
        this.params = params;
        this.expr = expr;
    }

    public List<Var> getParams() {
        return params;
    }

    public Expr getExpr() {
        return expr;
    }

    @Override
    public String toString() {
        return params.stream().map(Objects::toString).collect(Collectors.joining(" ")) + " -> " + expr;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expr == null) ? 0 : expr.hashCode());
        result = prime * result + ((params == null) ? 0 : params.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Lambda other = (Lambda) obj;
        if (expr == null) {
            if (other.expr != null)
                return false;
        } else if (!expr.equals(other.expr))
            return false;
        if (params == null) {
            if (other.params != null)
                return false;
        } else if (!params.equals(other.params))
            return false;
        return true;
    }
}
