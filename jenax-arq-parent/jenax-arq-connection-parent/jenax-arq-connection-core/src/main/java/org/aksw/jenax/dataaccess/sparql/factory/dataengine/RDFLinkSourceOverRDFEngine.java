package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.aksw.jenax.dataaccess.sparql.link.transform.LinkSparqlQueryTransformApp;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransform;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransforms;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.core.DatasetGraph;

public class RDFLinkSourceOverRDFEngine
    implements RDFLinkSource
{
    protected RDFEngine engine;

    protected RDFLinkSourceOverRDFEngine(RDFEngine engine) {
        super();
        this.engine = Objects.requireNonNull(engine);
    }

    public static RDFLinkSourceOverRDFEngine of(RDFEngine engine) {
        return new RDFLinkSourceOverRDFEngine(engine);
    }

    @Override
    public RDFLink newLink() {
        RDFLink link = engine.newLinkBuilder().build();

        DatasetGraph dataset = engine.getDataset();
        if (dataset != null) {
            RDFLinkTransform transform = RDFLinkTransforms.of(new LinkSparqlQueryTransformApp(dataset));
            link = transform.apply(link);
        }

        return link;
    }
}
