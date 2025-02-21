package org.aksw.jenax.dataaccess.deleted;

import org.aksw.jenax.dataaccess.sparql.link.common.RDFLinkUtils;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateTransform;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSourceWrapperBase;
import org.apache.jena.rdflink.LinkDatasetGraph;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.rdflink.LinkSparqlUpdate;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkModular;

@Deprecated // Use RDFLinkSourceWrapperLinkTransform with a RDFLinkTransformModular
public class RDFLinkSourceWrapperWithLinkTransformModular<X extends RDFLinkSource>
    extends RDFLinkSourceWrapperBase<X>
{
    protected LinkSparqlQueryTransform queryTransform;
    protected LinkSparqlUpdateTransform updateTransform;
    // protected LinkDatasetGraph datasetTransform;

    public RDFLinkSourceWrapperWithLinkTransformModular(X delegate,
            LinkSparqlQueryTransform queryTransform,
            LinkSparqlUpdateTransform updateTransform) {
        super(delegate);
        this.queryTransform = queryTransform;
        this.updateTransform = updateTransform;
    }

    @Override
    public RDFLink newLink() {
        RDFLink base = super.newLink();

        RDFLinkModular mod = RDFLinkUtils.asModular(base);
        LinkSparqlQuery q = mod.queryLink();
        LinkSparqlUpdate u = mod.updateLink();
        LinkDatasetGraph d = mod.datasetLink();

        if (queryTransform != null) {
            q = queryTransform.apply(q);
        }

        if (updateTransform != null) {
            u = updateTransform.apply(u);
        }

        RDFLink result = new RDFLinkModular(q, u, d);
        return result;
    }
}
