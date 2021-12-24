package org.aksw.dcat.jena.domain.api;

import java.util.Set;

import org.aksw.commons.util.obj.ObjectUtils;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.apache.jena.rdf.model.Resource;

/**
 * Binding of the core to jena
 *
 * @author raven Apr 9, 2018
 *
 */
public interface DcatDataset
    extends DcatEntity, DcatDatasetCore
{
    // @Iri("dcat:distribution")
    // Set<DcatDistribution> getDistribution();

    @Iri("dcat:distribution")
    // @Override
    <T extends Resource> Set<T> getDistributions(Class<T> clazz);

    default Set<? extends DcatDistribution> getDistributions2() {
        return getDistributions(DcatDistribution.class);
    }

//    default <T extends Resource> Collection<T> getDistributions(Class<T> clazz) {
//        return new SetFromPropertyValues<>(this, DCAT.distribution, clazz);
//    }
//
//    default Collection<? extends DcatDistribution> getDistributions() {
//        return getDistributions(DcatDistribution.class);
//    }

    public static String getLabel(DcatDataset ds) {
        String result = ObjectUtils.coalesce(
                ds::getTitle,
                ds::getIdentifier,
                () -> ds.isURIResource()
                    ? ds.getURI()
                    : Integer.toString(System.identityHashCode(ds)));

        return result;
    }
}
