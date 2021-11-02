package org.aksw.facete.v3.api;

import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.apache.jena.rdf.model.RDFNode;

public interface FacetedDataQuery<T extends RDFNode>
    extends DataQuery<T>
{
    FacetedQuery toFacetedQuery();
//    <U extends RDFNode> FacetedDataQuery<U> as(Class<U> clazz);

}
