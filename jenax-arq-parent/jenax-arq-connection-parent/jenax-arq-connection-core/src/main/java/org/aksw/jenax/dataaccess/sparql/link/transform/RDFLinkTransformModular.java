package org.aksw.jenax.dataaccess.sparql.link.transform;

import org.aksw.jenax.dataaccess.sparql.link.common.RDFLinkUtils;
import org.aksw.jenax.dataaccess.sparql.link.dataset.LinkDatasetGraphTransform;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateTransform;
import org.apache.jena.rdflink.LinkDatasetGraph;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.rdflink.LinkSparqlUpdate;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkModular;

public final class RDFLinkTransformModular
    implements RDFLinkTransform
{
    protected LinkSparqlQueryTransform queryTransform;
    protected LinkSparqlUpdateTransform updateTransform;
    protected LinkDatasetGraphTransform datasetTransform;

    public RDFLinkTransformModular(
            LinkSparqlQueryTransform queryTransform,
            LinkSparqlUpdateTransform updateTransform,
            LinkDatasetGraphTransform datasetTransform) {
        super();
        this.queryTransform = queryTransform;
        this.updateTransform = updateTransform;
        this.datasetTransform = datasetTransform;
    }

    public LinkSparqlQueryTransform getQueryTransform() {
        return queryTransform;
    }

    public LinkSparqlUpdateTransform getUpdateTransform() {
        return updateTransform;
    }

    public LinkDatasetGraphTransform getDatasetTransform() {
        return datasetTransform;
    }

    @Override
    public RDFLink apply(RDFLink base) {
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

        if (datasetTransform != null) {
            d = datasetTransform.apply(d);
        }

        RDFLink result = new RDFLinkModular(q, u, d);
        return result;
    }
}

