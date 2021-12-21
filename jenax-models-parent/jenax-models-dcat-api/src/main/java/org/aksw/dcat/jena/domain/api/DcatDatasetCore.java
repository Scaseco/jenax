package org.aksw.dcat.jena.domain.api;

import java.util.Collection;

public interface DcatDatasetCore
    extends DcatEntityCore
{
    /** Factory method for distributions - does not add them */
    DcatDistributionCore createDistribution();

    // <T> Collection<T /* extends DcatDistributionCore */> getDistributions(Class<T> clazz);
    Collection<String> getKeywords();


//	FoafAgent getPublisher();
//	VCardKind getContactPoint();
}
