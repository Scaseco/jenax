package org.aksw.facete.v3.api;

import java.util.Objects;
import java.util.Set;

import org.apache.jena.sparql.expr.Expr;

/** Facade to toggle an individual constraint on and off. */
public class FacetConstraintControlImpl
    implements FacetConstraintControl
{
    protected FacetConstraints container;
    protected Set<TreeQueryNode> references;
    protected Expr expr;

    public FacetConstraintControlImpl(FacetConstraints container, Set<TreeQueryNode> references, Expr expr) {
        super();
        this.container = container;
        this.references = references;
        this.expr = expr;
    }

    @Override
    public boolean enabled() {
        return Boolean.TRUE.equals(container.model.get(references, expr));
    }

    /** Remove from the container */
    public void unlink() {
        container.model.remove(references, expr);
    }

    public boolean isUnlinked() {
        return !container.model.contains(references, expr);
    }

    @Override
    public FacetConstraintControl enabled(boolean onOrOff) {
        container.model.put(references, expr, onOrOff);
        return this;
    }

    @Override
    public Expr expr() {
        return expr;
    }

    @Override
    public int hashCode() {
        return Objects.hash(container, expr, references);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FacetConstraintControlImpl other = (FacetConstraintControlImpl) obj;
        return Objects.equals(container, other.container) && Objects.equals(expr, other.expr)
                && Objects.equals(references, other.references);
    }
}
