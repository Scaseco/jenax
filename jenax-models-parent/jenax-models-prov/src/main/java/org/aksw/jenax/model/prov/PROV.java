package org.aksw.jenax.model.prov;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Provenance Ontology.
 *
 * There is probably some class like this one out there as a maven dep for provo.
 * Once we find it, we can make this class obsolete.
 *
 * @author raven
 *
 */
public class PROV {
    public static final Property Entity = ResourceFactory.createProperty(ProvTerms.Entity);
    public static final Property Activity = ResourceFactory.createProperty(ProvTerms.Activity);
    public static final Property Agent = ResourceFactory.createProperty(ProvTerms.Agent);
	
	
    public static final Property entity = ResourceFactory.createProperty(ProvTerms.entity);
    public static final Property hadUsage = ResourceFactory.createProperty(ProvTerms.hadUsage);
    public static final Property hadPlan = ResourceFactory.createProperty(ProvTerms.hadPlan);
    public static final Property hadGeneration = ResourceFactory.createProperty(ProvTerms.hadGeneration);
    public static final Property hadActivity = ResourceFactory.createProperty(ProvTerms.hadActivity);

    public static final Property qualifiedDerivation = ResourceFactory.createProperty(ProvTerms.qualifiedDerivation);
    public static final Property qualifiedAssociation = ResourceFactory.createProperty(ProvTerms.qualifiedAssociation);

    public static final Property hadPrimarySource = ResourceFactory.createProperty(ProvTerms.hadPrimarySource);
    public static final Property atTime = ResourceFactory.createProperty(ProvTerms.atTime);
    public static final Property startedAtTime = ResourceFactory.createProperty(ProvTerms.startedAtTime);
    public static final Property endedAtTime = ResourceFactory.createProperty(ProvTerms.endedAtTime);
    public static final Property wasGeneratedBy = ResourceFactory.createProperty(ProvTerms.wasGeneratedBy);
    public static final Property wasAssociatedWith = ResourceFactory.createProperty(ProvTerms.wasAssociatedWith);

}
