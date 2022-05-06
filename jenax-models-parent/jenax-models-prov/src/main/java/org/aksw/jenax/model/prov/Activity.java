package org.aksw.jenax.model.prov;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.Set;

import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Inverse;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.graph.Node;
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

    @Iri(ProvTerms.wasGeneratedBy)
    @Inverse
    @HashId
    Node getGeneratedNode();
    Activity setGeneratedNode(Node entity);

    @Iri(ProvTerms.wasGeneratedBy)
    @Inverse
    Entity getGenerated();
    Activity setGenerated(Resource entity);

    @Iri(ProvTerms.wasAssociatedWith)
    @HashId
    Entity getWasAssociatedWith();
    Activity setWasAssociatedWith(Resource entity);

    @Iri(ProvTerms.qualifiedAssociation)
    Set<QualifiedAssociation> getQualifiedAssociations();

    // Jena's type mapper by default does not have a registration for Instant (as of jena 4.3.2)

    @Iri(ProvTerms.startedAtTime)
    XSDDateTime getStartedAtTimeDt();
    Activity setStartedAtTimeDt(XSDDateTime dateTime);

    @Iri(ProvTerms.endedAtTime)
    XSDDateTime getEndedAtTimeDt();
    Activity setEndedAtTimeDt(XSDDateTime dateTime);

    // @Iri(ProvTerms.startedAtTime)
    default Instant getStartedAtTime() { return Instant.ofEpochSecond(getStartedAtTimeDt().getFullSeconds()); }
    default Activity setStartedAtTime(Instant instant) { return setStartedAtTimeDt(new XSDDateTime(GregorianCalendar.from(ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)))); }

    // @Iri(ProvTerms.endedAtTime)
    default Instant getEndedAtTime() { return Instant.ofEpochSecond(getEndedAtTimeDt().getFullSeconds()); }
    default Activity setEndedAtTime(Instant instant) { return setEndedAtTimeDt(new XSDDateTime(GregorianCalendar.from(ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)))); }

}
