package org.aksw.jenax.arq.anyresource;

import org.apache.jena.rdf.model.Resource;

/** Special purpose interface to allow treating even literals as Resources:
 *  {@code ModelFactory.createLiteral("test).as(AnyResource.class)}
 */
public interface AnyResource
    extends Resource
{
}