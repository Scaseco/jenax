package org.aksw.dcat.ap.domain.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

// TODO This class is what SpdxChecksum should be for Dcat-AP - consolidate!
@ResourceView
public interface Checksum
	extends Resource, ChecksumCore
{
	@Iri(Spdx._algorithm)
	String getAlgorithm();
	Checksum setAlgorithm(String algorithm);
	
	@Iri(Spdx._checksum)
	String getChecksum();
	Checksum setChecksum(String checksum);
}
