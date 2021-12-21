package org.aksw.dcat.jena.domain.api;

public interface DcatEntityCore {
//	String getCkanId();
//	void setCkanId(String id);

    String getIdentifier();
    DcatEntityCore setIdentifier(String name);

    /** A local name such as a human readable string in a CKAN catalog */
//    String getLocalName();
//    void setLocalName(String name);

    String getTitle();
    DcatEntityCore setTitle(String title);

    String getDescription();
    DcatEntityCore setDescription(String description);
}
