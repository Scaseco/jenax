package org.aksw.jenax.arq.datasource;

public interface RdfDataSourceSpecBasicMutable
    extends RdfDataSourceSpecBasic
{
    RdfDataSourceSpecBasicMutable setEngine(String engine);

    RdfDataSourceSpecBasicMutable setLocationContext(String locationContext);

    RdfDataSourceSpecBasicMutable setLocation(String location);

    RdfDataSourceSpecBasicMutable setTempDir(String location);

    RdfDataSourceSpecBasicMutable setLoader(String loader);

    /** If the db did not yet exist yet and had to be created, delete it after use? true = yes*/
    RdfDataSourceSpecBasicMutable setAutoDeleteIfCreated(Boolean onOrOff);
}