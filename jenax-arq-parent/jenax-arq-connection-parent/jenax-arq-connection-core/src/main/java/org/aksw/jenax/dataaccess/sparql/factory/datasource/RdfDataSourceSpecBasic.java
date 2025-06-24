package org.aksw.jenax.dataaccess.sparql.factory.datasource;

import org.aksw.jenax.dataaccess.sparql.creator.RDFDatabase;

public interface RdfDataSourceSpecBasic {
    String getEngine();

    String getLocationContext();

    String getLocation();
    // Path getLocation();

    String getTempDir();

    String getLoader();

    RDFDatabase getDatabase();

    /** If the db did not yet exist yet and had to be created, delete it after use? true = yes*/
    Boolean isAutoDeleteIfCreated();
}
