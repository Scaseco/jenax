package org.aksw.jena_sparql_api.rx.entity.engine;

import org.aksw.jena_sparql_api.rx.entity.model.EntityBaseQuery;
import org.aksw.jenax.sparql.fragment.impl.Concept;

public class EntityQueryFactory {
    public EntityBaseQuery createFromConcept(Concept concept) {
        EntityBaseQuery result = EntityBaseQuery.create(concept.getVar(), concept.asQuery());
        return result;
    }
}
