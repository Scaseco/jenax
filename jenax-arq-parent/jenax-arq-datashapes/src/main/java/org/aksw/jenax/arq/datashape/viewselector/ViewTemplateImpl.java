package org.aksw.jenax.arq.datashape.viewselector;

import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryImpl;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.apache.jena.rdf.model.Resource;

public class ViewTemplateImpl
    implements ViewTemplate
{
    /** An IRI resource that identifies the view template
        and possibly provides additional information in its model*/
    protected Resource metadata;

    protected Fragment1 condition;
    protected EntityQueryImpl entityQuery;

    public ViewTemplateImpl(Resource metadata, Fragment1 condition, EntityQueryImpl entityQuery) {
        super();
        this.metadata = metadata;
        this.condition = condition;
        this.entityQuery = entityQuery;
    }

    @Override
    public Resource getMetadata() {
        return metadata;
    }

    @Override
    public Fragment1 getCondition() {
        return condition;
    }

    @Override
    public EntityQueryImpl getEntityQuery() {
        return entityQuery;
    }
}
