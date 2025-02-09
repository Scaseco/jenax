package org.aksw.jenax.dataaccess.sparql.factory.datasource;

import org.aksw.jenax.dataaccess.sparql.creator.RdfDatabase;

public interface RdfDataSourceSpecBasic {
    String getEngine();

    String getLocationContext();

    String getLocation();

    String getTempDir();

    String getLoader();

    RdfDatabase getDatabase();

    /** If the db did not yet exist yet and had to be created, delete it after use? true = yes*/
    Boolean isAutoDeleteIfCreated();
}
