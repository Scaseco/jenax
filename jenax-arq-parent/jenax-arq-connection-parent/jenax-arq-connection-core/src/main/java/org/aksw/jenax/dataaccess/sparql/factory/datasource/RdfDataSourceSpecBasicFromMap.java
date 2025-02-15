package org.aksw.jenax.dataaccess.sparql.factory.datasource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.aksw.commons.collections.MapUtils;
import org.aksw.jenax.dataaccess.sparql.creator.RDFDatabase;

public class RdfDataSourceSpecBasicFromMap<X extends RdfDataSourceSpecBasicMutable<X>>
    implements RdfDataSourceSpecBasicMutable<X>
{
    protected Map<String, Object> map;

    protected RdfDataSourceSpecBasicFromMap() {
        this(new LinkedHashMap<>());
    }

    public RdfDataSourceSpecBasicFromMap(Map<String, Object> map) {
        super();
        this.map = map;
    }

    public static RdfDataSourceSpecBasicFromMap wrap(Map<String, Object> map) {
        return new RdfDataSourceSpecBasicFromMap(map);
    }

    public static RdfDataSourceSpecBasicFromMap create() {
        return wrap(new LinkedHashMap<>());
    }

    public Map<String, Object> getMap() {
        return map;
    }

    @Override
    public X setEngine(String engine) {
        MapUtils.putWithRemoveOnNull(map, RdfDataSourceSpecTerms.ENGINE_KEY, engine);
        return self();
    }

//    @Override
//    public Path getLocation() {
//        return (Path)map.get(RdfDataSourceSpecTerms.LOCATION_KEY);
//    }

    @Override
    public String getLocationContext() {
        return (String)map.get(RdfDataSourceSpecTerms.LOCATION_CONTEXT_KEY);
    }

    @Override
    public X setLocationContext(String context) {
        MapUtils.putWithRemoveOnNull(map, RdfDataSourceSpecTerms.LOCATION_CONTEXT_KEY, context);
        return self();
    }


    @Override
    public String getLocation() {
        return (String)map.get(RdfDataSourceSpecTerms.LOCATION_KEY);
    }

    @Override
    public X setLocation(String location) {
        MapUtils.putWithRemoveOnNull(map, RdfDataSourceSpecTerms.LOCATION_KEY, location);
        return self();
    }

    @Override
    public Boolean isAutoDeleteIfCreated() {
        return Optional.ofNullable((Boolean)map.get(RdfDataSourceSpecTerms.AUTO_DELETE_IF_CREATED_KEY))
                // .map(x -> (String)x)
                // .map(Boolean::parseBoolean)
                .orElse(false);
    }

    @Override
    public X setAutoDeleteIfCreated(Boolean onOrOff) {
        MapUtils.putWithRemoveOnNull(map, RdfDataSourceSpecTerms.AUTO_DELETE_IF_CREATED_KEY, onOrOff);
        // MapUtils.putWithRemoveOnNull(map, RdfDataSourceSpecTerms.AUTO_DELETE_IF_CREATED_KEY, Boolean.toString(onOrOff));
        return self();
    }

    @Override
    public String getTempDir() {
        return (String)map.get(RdfDataSourceSpecTerms.TEMP_DIR_KEY);
    }

    @Override
    public X setTempDir(String tempDir) {
        MapUtils.putWithRemoveOnNull(map, RdfDataSourceSpecTerms.TEMP_DIR_KEY, tempDir);
        return self();
    }

    @Override
    public String getEngine() {
        return (String)map.get(RdfDataSourceSpecTerms.ENGINE_KEY);
    }

    @Override
    public String getLoader() {
        return (String)map.get(RdfDataSourceSpecTerms.LOADER_KEY);
    }

    @Override
    public X setLoader(String loader) {
        MapUtils.putWithRemoveOnNull(map, RdfDataSourceSpecTerms.LOADER_KEY, loader);
        return self();
    }

    @Override
    public X setDatabase(RDFDatabase rdfDatabase) {
        MapUtils.putWithRemoveOnNull(map, RdfDataSourceSpecTerms.DATABASE_KEY, rdfDatabase);
        return self();
    }

    @Override
    public RDFDatabase getDatabase() {
        return (RDFDatabase)map.get(RdfDataSourceSpecTerms.DATABASE_KEY);
    }

    @Override
    public X setProperty(String key, Object value) {
        MapUtils.putWithRemoveOnNull(map, key, value);
        return self();
    }

    @Override
    public X setProperties(Map<String, Object> values) {
        values.forEach(this::setProperty);
        return self();
    }
}
