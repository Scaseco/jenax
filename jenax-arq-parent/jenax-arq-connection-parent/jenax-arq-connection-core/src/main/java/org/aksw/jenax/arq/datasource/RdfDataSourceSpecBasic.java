package org.aksw.jenax.arq.datasource;

public interface RdfDataSourceSpecBasic {
    String getEngine();

    String getLocationContext();

    String getLocation();

    String getTempDir();

    String getLoader();

    /** If the db did not yet exist yet and had to be created, delete it after use? true = yes*/
    Boolean isAutoDeleteIfCreated();
}
