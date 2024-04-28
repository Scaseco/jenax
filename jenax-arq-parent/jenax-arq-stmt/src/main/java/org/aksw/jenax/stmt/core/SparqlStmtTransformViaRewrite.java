package org.aksw.jenax.stmt.core;

import java.util.Objects;

import org.aksw.jenax.stmt.util.SparqlStmtUtils;
import org.apache.jena.sparql.algebra.optimize.Rewrite;

public class SparqlStmtTransformViaRewrite
    implements SparqlStmtTransform
{
    protected Rewrite rewrite;

    public SparqlStmtTransformViaRewrite(Rewrite rewrite) {
        super();
        this.rewrite = Objects.requireNonNull(rewrite);
    }

    public Rewrite getRewrite() {
        return rewrite;
    }

    @Override
    public SparqlStmt apply(SparqlStmt t) {
        SparqlStmt result = SparqlStmtUtils.applyOpTransform(t, rewrite::rewrite);
        return result;
    }
}
