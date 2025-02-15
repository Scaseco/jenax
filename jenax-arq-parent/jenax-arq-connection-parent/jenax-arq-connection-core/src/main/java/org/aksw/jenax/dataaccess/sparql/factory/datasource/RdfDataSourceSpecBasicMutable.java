package org.aksw.jenax.dataaccess.sparql.factory.datasource;

import java.util.Map;

import org.aksw.commons.util.obj.HasSelf;
import org.aksw.jenax.dataaccess.sparql.creator.RDFDatabase;

public interface RdfDataSourceSpecBasicMutable<X extends RdfDataSourceSpecBasicMutable<X>>
    extends RdfDataSourceSpecBasic, HasSelf<X>
{
    X setEngine(String engine);

    /**
     * Intended for use with virtual file systems.
     * If given then this is usually the URL of a file system against which to resolve the location.
     * Typically this can be left null.
     */
    X setLocationContext(String locationContext);

    /**
     * A specification of the location of the data for the data source.
     * Typically a file path or an URL.
     */
    X setLocation(String location);

    /**
     * Set a database for the engine.
     * The provider database must be supported by the builder implementation.
     */
    X setDatabase(RDFDatabase rdfDatabase);

    /** Set a specific directory to use for temporary data. */
    X setTempDir(String location);

    /** Hint for which strategy to use when loading data. */
    X setLoader(String loader);

    /** If the db did not yet exist yet and had to be created, delete it after use? true = yes*/
    X setAutoDeleteIfCreated(Boolean onOrOff);

    /** Set a vendor specific property. */
    X setProperty(String key, Object value);

    /** Set a vendor specific properties. */
    X setProperties(Map<String, Object> values);
}
