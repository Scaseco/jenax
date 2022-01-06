package org.aksw.dcat.jena.domain.api;

import java.util.Set;

import org.aksw.dcat.jena.term.DcatTerms;
import org.aksw.jena_sparql_api.mapper.annotation.Inverse;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;


public interface DcatDistribution
    extends DcatEntity, DcatDistributionCore
{
    /** Return the set of datasets this distribution is part of */
    @Inverse
    @Iri(DcatTerms.distribution)
    <T extends DcatDataset> Set<T> getDcatDatasets(Class<T> datasetClass);

    @Iri(DcatTerms.accessURL)
    @IriType
    Set<String> getAccessUrls();

    @Iri(DcatTerms.downloadURL)
    @IriType
    Set<String> getDownloadUrls();

    @Iri(DcatTerms.downloadURL)
    Set<DcatDownloadUrl> getDownloadUrlResources();


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
