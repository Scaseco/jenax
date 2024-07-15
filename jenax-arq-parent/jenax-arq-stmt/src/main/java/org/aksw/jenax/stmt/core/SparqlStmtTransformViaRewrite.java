package org.aksw.jenax.stmt.core;

import java.util.Objects;

import org.aksw.jenax.stmt.util.SparqlStmtUtils;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.optimize.Rewrite;

/**
 * Class that captures SPARQL statement transforms based on {@link Rewrite}.
 *
 * There is special handling for this class which attempts to group multiple rewrites into a single larger on
 * in order to minimize round trips between {@link Query} and {@link Op}.
 */
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
