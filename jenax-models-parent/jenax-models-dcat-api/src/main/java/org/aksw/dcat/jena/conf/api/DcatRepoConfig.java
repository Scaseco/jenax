package org.aksw.dcat.jena.conf.api;

import java.util.Map;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.KeyIri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
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

    @IriNs("eg")
    @KeyIri("http://dcat.aksw.org/key")
    Map<String, String> getProperties();
}
