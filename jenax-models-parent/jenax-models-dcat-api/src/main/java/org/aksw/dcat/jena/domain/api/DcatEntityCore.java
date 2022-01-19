package org.aksw.dcat.jena.domain.api;

import org.aksw.jenax.annotation.reprogen.IriNs;

public interface DcatEntityCore {
//	String getCkanId();
//	void setCkanId(String id);

    @IriNs("dcterms")
    String getIdentifier();
    DcatEntityCore setIdentifier(String name);

    /** A local name such as a human readable string in a CKAN catalog */
//    String getLocalName();
//    void setLocalName(String name);

    @IriNs("dcterms")
    String getTitle();
    DcatEntityCore setTitle(String title);

    @IriNs("dcterms")
    String getDescription();
    DcatEntityCore setDescription(String description);
}
