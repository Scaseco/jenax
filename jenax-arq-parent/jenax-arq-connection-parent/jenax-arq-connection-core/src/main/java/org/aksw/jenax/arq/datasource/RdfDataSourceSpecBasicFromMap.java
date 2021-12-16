package org.aksw.jenax.arq.datasource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.aksw.commons.collections.MapUtils;

public class RdfDataSourceSpecBasicFromMap
    implements RdfDataSourceSpecBasicMutable
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
    public RdfDataSourceSpecBasicMutable setEngine(String engine) {
        MapUtils.putWithRemoveOnNull(map, RdfDataSourceSpecTerms.ENGINE_KEY, engine);
        return this;
    }

    @Override
    public String getLocationContext() {
        return (String)map.get(RdfDataSourceSpecTerms.LOCATION_CONTEXT_KEY);
    }

    @Override
    public RdfDataSourceSpecBasicMutable setLocationContext(String locationContext) {
        MapUtils.putWithRemoveOnNull(map, RdfDataSourceSpecTerms.LOCATION_CONTEXT_KEY, locationContext);
        return this;
    }

    @Override
    public String getLocation() {
        return (String)map.get(RdfDataSourceSpecTerms.LOCATION_KEY);
    }

    @Override
    public RdfDataSourceSpecBasicMutable setLocation(String location) {
        MapUtils.putWithRemoveOnNull(map, RdfDataSourceSpecTerms.LOCATION_KEY, location);
        return this;
    }

    @Override
    public Boolean isAutoDeleteIfCreated() {
        return Optional.ofNullable(map.get(RdfDataSourceSpecTerms.AUTO_DELETE_IF_CREATED_KEY))
                .map(x -> (String)x)
                .map(Boolean::parseBoolean)
                .orElse(false);
    }

    @Override
    public RdfDataSourceSpecBasicMutable setAutoDeleteIfCreated(Boolean onOrOff) {
        MapUtils.putWithRemoveOnNull(map, RdfDataSourceSpecTerms.AUTO_DELETE_IF_CREATED_KEY, Boolean.toString(onOrOff));
        return this;
    }

    @Override
    public String getTempDir() {
        return (String)map.get(RdfDataSourceSpecTerms.TEMP_DIR_KEY);
    }

    @Override
    public RdfDataSourceSpecBasicMutable setTempDir(String tempDir) {
        MapUtils.putWithRemoveOnNull(map, RdfDataSourceSpecTerms.TEMP_DIR_KEY, tempDir);
        return this;
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
    public RdfDataSourceSpecBasicMutable setLoader(String loader) {
        MapUtils.putWithRemoveOnNull(map, RdfDataSourceSpecTerms.LOADER_KEY, loader);
        return this;
    }
}