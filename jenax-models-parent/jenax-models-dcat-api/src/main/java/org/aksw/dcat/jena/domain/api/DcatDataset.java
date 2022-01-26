package org.aksw.dcat.jena.domain.api;

import java.util.Collection;
import java.util.Set;

import org.aksw.commons.util.obj.ObjectUtils;
import org.aksw.jenax.annotation.reprogen.Iri;
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
    <T extends Resource> Set<T> getDistributionsAs(Class<T> clazz);

    default Set<DcatDistribution> getDistributions() {
        return getDistributionsAs(DcatDistribution.class);
    }

    default DcatDistribution addNewDistribution(String iri) {
    	DcatDistribution result = getModel().createResource(iri).as(DcatDistribution.class);
    	getDistributions().add(result);
    	return result;
    }
    
    @Iri("dcterms:keyword")
    @Override
    Collection<String> getKeywords();

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
