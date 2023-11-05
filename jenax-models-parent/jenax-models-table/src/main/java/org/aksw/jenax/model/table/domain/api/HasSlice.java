package org.aksw.jenax.model.table.domain.api;

import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface HasSlice
    extends Resource
{
    Long getOffset();
    HasSlice setOffest();

    Long getLimit();
    HasSlice setLimit();
}
