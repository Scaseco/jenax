package org.aksw.jenax.model.prov;

import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface QualifiedDerivation
    extends ProvComponent
{
    @Iri(ProvTerms.entity)
    @HashId
    Entity getEntity();
    QualifiedDerivation setEntity(Resource entity);

    @Iri(ProvTerms.hadActivity)
    @HashId
    Activity getHadActivity();
    QualifiedDerivation setHadActivity(Resource activity);

    default Activity getOrSetHadActivity() {
        return JenaPluginUtils.getOrSet(this, Activity.class, this::getHadActivity, this::setHadActivity);
    }

    @Iri(ProvTerms.hadUsage)
    Activity getHadUsage();
    QualifiedDerivation setHadUsage(Resource activity);

    @Iri(ProvTerms.hadGeneration)
    Activity getHadGeneration();
    QualifiedDerivation setHadGeneration(Resource activity);
}
