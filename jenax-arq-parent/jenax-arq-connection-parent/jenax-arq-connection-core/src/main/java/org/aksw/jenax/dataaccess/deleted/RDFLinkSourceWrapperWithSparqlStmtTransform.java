package org.aksw.jenax.dataaccess.deleted;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.link.common.RDFLinkUtils;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSourceWrapperOverNewLinkBase;
import org.aksw.jenax.stmt.core.SparqlStmtTransform;
import org.apache.jena.rdflink.RDFLink;

public class RDFLinkSourceWrapperWithSparqlStmtTransform<X extends RDFLinkSource>
    extends RDFLinkSourceWrapperOverNewLinkBase<X>
{
    protected SparqlStmtTransform stmtTransform;

    public RDFLinkSourceWrapperWithSparqlStmtTransform(X delegate, SparqlStmtTransform stmtTransform) {
        super(delegate);
        this.stmtTransform = Objects.requireNonNull(stmtTransform);
    }

    @Override
    public RDFLink buildLink() {
        RDFLink base = super.newLink();
        RDFLink result = RDFLinkUtils.wrapWithStmtTransform(base, stmtTransform);
        return result;
    }

    @Override
    public String toString() {
        return "RDFLinkSourceWrapperWithSparqlStmtTransform [stmtTransform=" + stmtTransform + ", getDelegate()=" + getDelegate() + "]";
    }
}
