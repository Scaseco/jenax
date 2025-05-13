package jenax.engine.qlever.docker;

public interface QleverConfig {

    String getIndexBaseName();

    void setIndexBaseName(String indexBaseName);

    Integer getPort();

    void setPort(Integer port);

    String getAccessToken();

    void setAccessToken(String accessToken);

    Integer getNumSimultaneousQueries();

    void setNumSimultaneousQueries(Integer numSimultaneousQueries);

    String getMemoryMaxSize();

    void setMemoryMaxSize(String memoryMaxSize);

    String getCacheMaxSize();

    void setCacheMaxSize(String cacheMaxSize);

    String getCacheMaxSizeSingleEntry();

    void setCacheMaxSizeSingleEntry(String cacheMaxSizeSingleEntry);

    String getLazyResultMaxCacheSize();

    void setLazyResultMaxCacheSize(String lazyResultMaxCacheSize);

    Long getCacheMaxNumEntries();

    void setCacheMaxNumEntries(Long cacheMaxNumEntries);

    Boolean getNoPatterns();

    void setNoPatterns(Boolean noPatterns);

    Boolean getNoPatternTrick();

    void setNoPatternTrick(Boolean noPatternTrick);

    Boolean getText();

    void setText(Boolean text);

    Boolean getOnlyPsoAndPosPermutations();

    void setOnlyPsoAndPosPermutations(Boolean onlyPsoAndPosPermutations);

    String getDefaultQueryTimeout();

    void setDefaultQueryTimeout(String defaultQueryTimeout);

    Long getServiceMaxValueRows();

    void setServiceMaxValueRows(Long serviceMaxValueRows);

    Boolean getThrowOnUnboundVariables();

    void setThrowOnUnboundVariables(Boolean throwOnUnboundVariables);

    default void copyInto(QleverConfig dest, boolean copyNulls) {
        String str;
        Boolean b;
        Integer i;
        Long l;

//        if ((str = getDbPath()) != null || copyNulls) {
//            dest.setDbPath(str);
//        }
        if((str = getIndexBaseName()) != null || copyNulls) {
            dest.setIndexBaseName(str);
        }
        if((i = getPort()) != null || copyNulls) {
            dest.setPort(i);
        }
        if((str = getAccessToken()) != null || copyNulls) {
            dest.setAccessToken(str);
        }
        if((i = getNumSimultaneousQueries()) != null || copyNulls) {
            dest.setNumSimultaneousQueries(i);
        }
        if((str = getMemoryMaxSize()) != null || copyNulls) {
            dest.setMemoryMaxSize(str);
        }
        if((str = getCacheMaxSize()) != null || copyNulls) {
            dest.setCacheMaxSize(str);
        }
        if((str = getCacheMaxSizeSingleEntry()) != null || copyNulls) {
            dest.setCacheMaxSizeSingleEntry(str);
        }
        if((str = getLazyResultMaxCacheSize()) != null || copyNulls) {
            dest.setLazyResultMaxCacheSize(str);
        }
        if((l = getCacheMaxNumEntries()) != null || copyNulls) {
            dest.setCacheMaxNumEntries(l);
        }
        if((b = getNoPatterns()) != null || copyNulls) {
            dest.setNoPatterns(b);
        }
        if((b = getNoPatternTrick()) != null || copyNulls) {
            dest.setNoPatternTrick(b);
        }
        if((b = getText()) != null || copyNulls) {
            dest.setText(b);
        }
        if((l = getServiceMaxValueRows()) != null || copyNulls) {
            dest.setServiceMaxValueRows(l);
        }
        if((b = getThrowOnUnboundVariables()) != null || copyNulls) {
            dest.setThrowOnUnboundVariables(b);
        }
    }
}
