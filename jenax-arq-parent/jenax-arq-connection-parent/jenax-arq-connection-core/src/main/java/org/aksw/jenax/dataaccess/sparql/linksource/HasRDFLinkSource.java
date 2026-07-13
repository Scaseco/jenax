package org.aksw.jenax.dataaccess.sparql.linksource;

/**
 * Interface for entities that provide an RDFLinkSource.
 */
@FunctionalInterface
public interface HasRDFLinkSource {
    RDFLinkSource getLinkSource();

    // XXX Could add shorthands for newLink and newLinkBuilder that delegate
    //     to the link source without 'this' being an RDFLinkSource itself.
}
