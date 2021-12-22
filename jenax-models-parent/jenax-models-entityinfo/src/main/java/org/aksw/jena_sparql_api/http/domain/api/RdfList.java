package org.aksw.jena_sparql_api.http.domain.api;

import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
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
