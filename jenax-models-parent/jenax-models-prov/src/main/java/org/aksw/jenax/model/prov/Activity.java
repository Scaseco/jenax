package org.aksw.jenax.model.prov;

import java.time.Instant;
import java.util.Set;

import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface Activity
    extends ProvComponent
{
    @Iri(ProvTerms.hadPlan)
    @HashId
    Plan getHadPlan();
    Activity setHadPlan(Resource plan);

    default Plan getOrSetHadPlan() {
        return JenaPluginUtils.getOrSet(this, Plan.class, this::getHadPlan, this::setHadPlan);
    }


    @Iri(ProvTerms.wasAssociatedWith)
    @HashId
    Entity getWasAssociatedWith();
    Activity setWasAssociatedWith(Resource entity);

    @Iri(ProvTerms.qualifiedAssociation)
    Set<QualifiedAssociation> getQualifiedAssociations();

    @Iri(ProvTerms.startedAtTime)
    Instant getStartedAtTime();
    Activity setStartedAtTime(Instant instant);

    @Iri(ProvTerms.startedAtTime)
    Instant getEndedAtTime();
    Activity setEndedAtTime(Instant instant);

}
