package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefCatalog;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.RdfType;
import org.aksw.jenax.annotation.reprogen.ResourceView;

/**
 * This class differs from DataRefDcat by the level of indirection:
 * DataRefDcat has a copy of the record, while this class only refers to any entry in another
 * catalog
 *
 * @author raven
 *
 */
@ResourceView
@RdfType("rpif:DataRefCatalog")
public interface RdfDataRefCatalog
    extends DataRefCatalog, RdfDataRef
{
    @Override
    @IriNs("eg")
    RdfDataRef getCatalogDataRef();
    RdfDataRefCatalog setCatalogDataRef(RdfDataRef dataRef);

    @IriNs("eg")
    RdfDataRefCatalog setEntryId(String entryId);

    @Override
    default <T> T acceptRdf(RdfDataRefVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
