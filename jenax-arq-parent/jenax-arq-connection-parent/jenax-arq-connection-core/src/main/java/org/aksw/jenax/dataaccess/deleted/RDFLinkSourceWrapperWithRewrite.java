package org.aksw.jenax.dataaccess.deleted;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.link.common.RDFLinkUtils;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSourceWrapperOverNewLinkBase;
import org.aksw.jenax.stmt.util.SparqlStmtUtils;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.algebra.optimize.Rewrite;

public class RDFLinkSourceWrapperWithRewrite<X extends RDFLinkSource>
    extends RDFLinkSourceWrapperOverNewLinkBase<X>
{
    protected Rewrite rewrite;

    public RDFLinkSourceWrapperWithRewrite(X delegate, Rewrite rewrite) {
        super(delegate);
        this.rewrite = Objects.requireNonNull(rewrite);
    }

    public Rewrite getRewrite() {
        return rewrite;
    }

    @Override
    public RDFLink buildLink() {
        RDFLink base = super.newLink();
        RDFLink result = RDFLinkUtils.wrapWithStmtTransform(base, stmt -> SparqlStmtUtils.applyOpTransform(stmt, rewrite::rewrite));
        return result;
    }

    @Override
    public String toString() {
        return "RDFLinkSourceWrapperWithRewrite [rewrite=" + rewrite + ", getDelegate()=" + getDelegate() + "]";
    }
}
