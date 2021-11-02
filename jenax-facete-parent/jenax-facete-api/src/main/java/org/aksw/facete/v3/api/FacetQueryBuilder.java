package org.aksw.facete.v3.api;

import org.apache.jena.rdf.model.RDFNode;

public interface FacetQueryBuilder<T extends RDFNode> {
    FacetDirNode parent();
    FacetQueryBuilder<T> withFocusCounts(boolean onOrOff);
    FacetQueryBuilder<T> withDistinctValueCounts(boolean onOrOff);

    default FacetQueryBuilder<T> withFocusCounts() {
        return withFocusCounts(true);
    }

    default FacetQueryBuilder<T> withDistinctValueCounts() {
        return withFocusCounts(true);
    }

    <X extends RDFNode> FacetValueQueryBuilder<X> itemsAs(Class<X> itemClazz);

    FacetedDataQuery<T> query2();
}
