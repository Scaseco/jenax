package org.aksw.jenax.dataaccess.sparql.link.dataset;

import java.util.function.Function;

import org.apache.jena.rdflink.LinkDatasetGraph;

public interface LinkDatasetGraphTransform
    extends Function<LinkDatasetGraph, LinkDatasetGraph>
{
}
