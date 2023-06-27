package org.aksw.jenax.arq.datashape.viewselector;

import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryImpl;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.apache.jena.rdf.model.Resource;

public class ViewTemplateImpl
    implements ViewTemplate
{
    /** An IRI resource that identifies the view template
        and possibly provides additional information in its model*/
    protected Resource metadata;

    protected UnaryRelation condition;
    protected EntityQueryImpl entityQuery;

    public ViewTemplateImpl(Resource metadata, UnaryRelation condition, EntityQueryImpl entityQuery) {
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
    public UnaryRelation getCondition() {
        return condition;
    }

    @Override
    public EntityQueryImpl getEntityQuery() {
        return entityQuery;
    }
}
