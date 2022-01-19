package org.aksw.jena_sparql_api.http.domain.api;

import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

/**
 * Helper class to generate HashIds from RdfLists
 *
 * @author raven
 *
 */
@ResourceView
public interface RdfList
    extends Resource
{
    @IriNs("rdf")
    @HashId
    Resource getFirst();
    RdfList setFirst(Resource first);

    @IriNs("rdf")
    @HashId
    RdfList getRest();
    RdfList setRest(Resource rest);
}
