package org.aksw.jenax.arq.datashape.viewselector;

import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryImpl;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.apache.jena.rdf.model.Resource;

public interface ViewTemplate
{
    Resource getMetadata();

    /**
     * A SPARQL graph pattern holding the condition a resource must satisfy in order
     * to qualify as applicable to the view
     *
     * @return
     */
    UnaryRelation getCondition();

    /**
     * The SPARQL-based entity query which yields for a resource
     * the corresponding graph fragments
     *
     *
     * TODO This method should not return a full query but just the part the specifying the attributes
     *
     * @return
     */
    EntityQueryImpl getEntityQuery();
}
