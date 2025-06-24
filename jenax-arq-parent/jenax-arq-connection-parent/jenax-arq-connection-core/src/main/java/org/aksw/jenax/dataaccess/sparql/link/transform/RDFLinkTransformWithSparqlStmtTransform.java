package org.aksw.jenax.dataaccess.sparql.link.transform;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.link.common.RDFLinkUtils;
import org.aksw.jenax.stmt.core.SparqlStmtTransform;
import org.apache.jena.rdflink.RDFLink;

public class RDFLinkTransformWithSparqlStmtTransform
    implements RDFLinkTransform
{
    protected SparqlStmtTransform stmtTransform;

    public RDFLinkTransformWithSparqlStmtTransform(SparqlStmtTransform stmtTransform) {
        super();
        this.stmtTransform = Objects.requireNonNull(stmtTransform);
    }

    @Override
    public RDFLink apply(RDFLink base) {
        RDFLink result = RDFLinkUtils.wrapWithStmtTransform(base, stmtTransform);
        return result;
    }
}
