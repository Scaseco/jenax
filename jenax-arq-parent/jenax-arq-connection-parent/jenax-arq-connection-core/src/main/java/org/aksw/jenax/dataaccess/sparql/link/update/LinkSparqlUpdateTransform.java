package org.aksw.jenax.dataaccess.sparql.link.update;

import java.util.function.Function;

import org.apache.jena.rdflink.LinkSparqlUpdate;

public interface LinkSparqlUpdateTransform
    extends Function<LinkSparqlUpdate, LinkSparqlUpdate>
{
}
