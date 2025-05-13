package jenax.engine.qlever.docker;

import java.io.Serializable;
import java.util.Objects;

public class QleverConfRun
    implements Serializable, QleverConfig
{
    private static final long serialVersionUID = 1L;

    // protected String dbPath;
    protected String indexBaseName;
    protected Integer port;
    protected String accessToken; // = "";
    protected Integer numSimultaneousQueries; // = 1;
    protected String memoryMaxSize;
    protected String cacheMaxSize;
    protected String cacheMaxSizeSingleEntry;
    protected String lazyResultMaxCacheSize;
    protected Long cacheMaxNumEntries;
    protected Boolean noPatterns;
    protected Boolean noPatternTrick;
    protected Boolean text;
    protected Boolean onlyPsoAndPosPermutations;
    protected String defaultQueryTimeout;
    protected Long serviceMaxValueRows;
    protected Boolean throwOnUnboundVariables;

//    public String getDbPath() {
//        return dbPath;
//    }
//
//    public void setDbPath(String dbPath) {
//        this.dbPath = dbPath;
//    }

    @Override
    public String getIndexBaseName() {
        return indexBaseName;
    }

    @Override
    public void setIndexBaseName(String indexBaseName) {
        this.indexBaseName = indexBaseName;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    @Override
    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public Integer getNumSimultaneousQueries() {
        return numSimultaneousQueries;
    }

    @Override
    public void setNumSimultaneousQueries(Integer numSimultaneousQueries) {
        this.numSimultaneousQueries = numSimultaneousQueries;
    }

    @Override
    public String getMemoryMaxSize() {
        return memoryMaxSize;
    }

    @Override
    public void setMemoryMaxSize(String memoryMaxSize) {
        this.memoryMaxSize = memoryMaxSize;
    }

    @Override
    public String getCacheMaxSize() {
        return cacheMaxSize;
    }

    @Override
    public void setCacheMaxSize(String cacheMaxSize) {
        this.cacheMaxSize = cacheMaxSize;
    }

    @Override
    public String getCacheMaxSizeSingleEntry() {
        return cacheMaxSizeSingleEntry;
    }

    @Override
    public void setCacheMaxSizeSingleEntry(String cacheMaxSizeSingleEntry) {
        this.cacheMaxSizeSingleEntry = cacheMaxSizeSingleEntry;
    }

    @Override
    public String getLazyResultMaxCacheSize() {
        return lazyResultMaxCacheSize;
    }

    @Override
    public void setLazyResultMaxCacheSize(String lazyResultMaxCacheSize) {
        this.lazyResultMaxCacheSize = lazyResultMaxCacheSize;
    }

    @Override
    public Long getCacheMaxNumEntries() {
        return cacheMaxNumEntries;
    }

    @Override
    public void setCacheMaxNumEntries(Long cacheMaxNumEntries) {
        this.cacheMaxNumEntries = cacheMaxNumEntries;
    }

    @Override
    public Boolean getNoPatterns() {
        return noPatterns;
    }

    @Override
    public void setNoPatterns(Boolean noPatterns) {
        this.noPatterns = noPatterns;
    }

    @Override
    public Boolean getNoPatternTrick() {
        return noPatternTrick;
    }

    @Override
    public void setNoPatternTrick(Boolean noPatternTrick) {
        this.noPatternTrick = noPatternTrick;
    }

    @Override
    public Boolean getText() {
        return text;
    }

    @Override
    public void setText(Boolean text) {
        this.text = text;
    }

    @Override
    public Boolean getOnlyPsoAndPosPermutations() {
        return onlyPsoAndPosPermutations;
    }

    @Override
    public void setOnlyPsoAndPosPermutations(Boolean onlyPsoAndPosPermutations) {
        this.onlyPsoAndPosPermutations = onlyPsoAndPosPermutations;
    }

    @Override
    public String getDefaultQueryTimeout() {
        return defaultQueryTimeout;
    }

    @Override
    public void setDefaultQueryTimeout(String defaultQueryTimeout) {
        this.defaultQueryTimeout = defaultQueryTimeout;
    }

    @Override
    public Long getServiceMaxValueRows() {
        return serviceMaxValueRows;
    }

    @Override
    public void setServiceMaxValueRows(Long serviceMaxValueRows) {
        this.serviceMaxValueRows = serviceMaxValueRows;
    }

    @Override
    public Boolean getThrowOnUnboundVariables() {
        return throwOnUnboundVariables;
    }

    @Override
    public void setThrowOnUnboundVariables(Boolean throwOnUnboundVariables) {
        this.throwOnUnboundVariables = throwOnUnboundVariables;
    }

    @Override
    public String toString() {
        return "QleverConfRun [indexBaseName=" + indexBaseName + ", port=" + port
                + ", accessToken=" + accessToken + ", numSimultaneousQueries=" + numSimultaneousQueries
                + ", memoryMaxSize=" + memoryMaxSize + ", cacheMaxSize=" + cacheMaxSize + ", cacheMaxSizeSingleEntry="
                + cacheMaxSizeSingleEntry + ", lazyResultMaxCacheSize=" + lazyResultMaxCacheSize
                + ", cacheMaxNumEntries=" + cacheMaxNumEntries + ", noPatterns=" + noPatterns + ", noPatternTrick="
                + noPatternTrick + ", text=" + text + ", onlyPsoAndPosPermutations=" + onlyPsoAndPosPermutations
                + ", defaultQueryTimeout=" + defaultQueryTimeout + ", serviceMaxValueRows=" + serviceMaxValueRows
                + ", throwOnUnboundVariables=" + throwOnUnboundVariables + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, cacheMaxNumEntries, cacheMaxSize, cacheMaxSizeSingleEntry,
                defaultQueryTimeout, indexBaseName, lazyResultMaxCacheSize, memoryMaxSize, noPatternTrick, noPatterns,
                numSimultaneousQueries, onlyPsoAndPosPermutations, port, serviceMaxValueRows, text,
                throwOnUnboundVariables);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        QleverConfRun other = (QleverConfRun) obj;
        return Objects.equals(accessToken, other.accessToken)
                && Objects.equals(cacheMaxNumEntries, other.cacheMaxNumEntries)
                && Objects.equals(cacheMaxSize, other.cacheMaxSize)
                && Objects.equals(cacheMaxSizeSingleEntry, other.cacheMaxSizeSingleEntry)
                && Objects.equals(defaultQueryTimeout, other.defaultQueryTimeout)
                && Objects.equals(indexBaseName, other.indexBaseName)
                && Objects.equals(lazyResultMaxCacheSize, other.lazyResultMaxCacheSize)
                && Objects.equals(memoryMaxSize, other.memoryMaxSize)
                && Objects.equals(noPatternTrick, other.noPatternTrick) && Objects.equals(noPatterns, other.noPatterns)
                && Objects.equals(numSimultaneousQueries, other.numSimultaneousQueries)
                && Objects.equals(onlyPsoAndPosPermutations, other.onlyPsoAndPosPermutations)
                && Objects.equals(port, other.port) && Objects.equals(serviceMaxValueRows, other.serviceMaxValueRows)
                && Objects.equals(text, other.text)
                && Objects.equals(throwOnUnboundVariables, other.throwOnUnboundVariables);
    }
}
