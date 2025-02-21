package org.aksw.jenax.dataaccess.sparql.link.transform;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.sparql.core.DatasetGraph;

public class LinkSparqlQueryTransformApp
    implements LinkSparqlQueryTransform
{
    protected DatasetGraph datasetGraph;

    public LinkSparqlQueryTransformApp(DatasetGraph datasetGraph) {
        super();
        this.datasetGraph = Objects.requireNonNull(datasetGraph);
    }

    @Override
    public LinkSparqlQuery apply(LinkSparqlQuery baseLink) {
        LinkSparqlQuery result = new LinkSparqlQueryApp(baseLink, datasetGraph);
        return result;
   }
}

