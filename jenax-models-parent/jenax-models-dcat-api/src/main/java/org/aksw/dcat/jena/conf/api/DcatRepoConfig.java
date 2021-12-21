package org.aksw.dcat.jena.conf.api;

import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.rdf.model.Resource;

/**
 * Class for a local dcat repository configuration
 *
 * May eventually be moved to a higher level package once the architecture
 * stabilizes.
 *
 * @author raven
 *
 */
@ResourceView
public interface DcatRepoConfig
    extends Resource
{
    @IriNs("eg")
    String getEngine();
    DcatRepoConfig setEngine(String engine);

    @IriNs("eg")
    Resource getEngineConf();
    DcatRepoConfig setEngineConf(Resource resource);
}
