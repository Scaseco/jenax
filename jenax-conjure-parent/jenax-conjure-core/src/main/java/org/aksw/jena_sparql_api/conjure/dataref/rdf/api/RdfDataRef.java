package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRef;
import org.apache.jena.rdf.model.Resource;

/**
 * Base class for DataRefs backed by RDF
 *
 * @author raven
 *
 */
public interface RdfDataRef
    extends Resource, DataRef
{
    <T> T acceptRdf(RdfDataRefVisitor<T> visitor);
}
