package org.aksw.facete.v3.api;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

// class FacetConstraint {
// }

/** A collection of constraints */
public class FacetConstraints {
    // protected Map<Expr, Boolean> exprToState;

    // TODO Should the final value be a wrapper that links back to the keys?
    protected Table<Set<TreeQueryNode>, Expr, Boolean> model = HashBasedTable.create();

    protected TreeQuery treeQuery;

    public FacetConstraints() {
        this.treeQuery = new TreeQueryImpl();
    }

    public FacetConstraints(TreeQuery treeQuery) {
        this.treeQuery = treeQuery;
    }

    public Collection<Expr> getExprs() {
        return model.columnKeySet();
    }

    public TreeQuery getTreeQuery() {
        return treeQuery;
    }

    public ConstraintApiImpl getFacade(TreeQueryNode node) {
        return new ConstraintApiImpl(this, node);
    }

    @Override
    public String toString() {
        return Objects.toString(model);
    }
}
