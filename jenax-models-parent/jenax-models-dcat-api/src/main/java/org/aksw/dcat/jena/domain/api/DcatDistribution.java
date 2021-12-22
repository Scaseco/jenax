package org.aksw.dcat.jena.domain.api;

import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Inverse;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;


public interface DcatDistribution
    extends DcatEntity, DcatDistributionCore
{
    /** Return the set of datasets this distribution is part of */
    @Inverse
    @Iri("dcat:distribution")
    <T extends DcatDataset> Set<T> getDcatDatasets(Class<T> datasetClass);

    @Iri("dcat:accessURL")
    @IriType
    Set<String> getAccessUrls();

    @Iri("dcat:downloadURL")
    @IriType
    Set<String> getDownloadUrls();

//	Collection<Resource> getAccessResources();
//	Collection<Resource> getDownloadResources();
//
//	default Collection<String> getAccessUrls() {
//		Collection<String> result = new CollectionFromConverter<>(getAccessResources(),
//				Converter.from(getModel()::createResource, Resource::getURI));
//		return result;
//	}
//
//	default Collection<String> getDownloadUrls() {
//		Collection<String> result = new CollectionFromConverter<>(getDownloadResources(),
//				Converter.from(getModel()::createResource, Resource::getURI));
//		return result;
//	}

//	default SpdxChecksum getChecksum() {
//		return null;
//		//ResourceUtils.getProperty(this, Spdx.ge)
//	}
}
