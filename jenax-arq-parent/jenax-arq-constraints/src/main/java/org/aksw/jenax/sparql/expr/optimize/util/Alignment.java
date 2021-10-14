package org.aksw.jenax.sparql.expr.optimize.util;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;

import org.apache.jena.sparql.expr.Expr;

public class Alignment
    extends SimpleImmutableEntry<List<Expr>, List<Expr>>
{
    private static final long serialVersionUID = 1L;

    public Alignment(List<Expr> key, List<Expr> value) {
        super(key, value);
    }

    public boolean isSameSize() {
        return this.getKey().size() == this.getValue().size();
    }
}