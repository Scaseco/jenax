package jenax.engine.qlever.docker;

import java.io.Serializable;
import java.util.Objects;

public class QleverConfRun
    implements Serializable
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

    public String getIndexBaseName() {
        return indexBaseName;
    }

    public void setIndexBaseName(String indexBaseName) {
        this.indexBaseName = indexBaseName;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Integer getNumSimultaneousQueries() {
        return numSimultaneousQueries;
    }

    public void setNumSimultaneousQueries(Integer numSimultaneousQueries) {
        this.numSimultaneousQueries = numSimultaneousQueries;
    }

    public String getMemoryMaxSize() {
        return memoryMaxSize;
    }

    public void setMemoryMaxSize(String memoryMaxSize) {
        this.memoryMaxSize = memoryMaxSize;
    }

    public String getCacheMaxSize() {
        return cacheMaxSize;
    }

    public void setCacheMaxSize(String cacheMaxSize) {
        this.cacheMaxSize = cacheMaxSize;
    }

    public String getCacheMaxSizeSingleEntry() {
        return cacheMaxSizeSingleEntry;
    }

    public void setCacheMaxSizeSingleEntry(String cacheMaxSizeSingleEntry) {
        this.cacheMaxSizeSingleEntry = cacheMaxSizeSingleEntry;
    }

    public String getLazyResultMaxCacheSize() {
        return lazyResultMaxCacheSize;
    }

    public void setLazyResultMaxCacheSize(String lazyResultMaxCacheSize) {
        this.lazyResultMaxCacheSize = lazyResultMaxCacheSize;
    }

    public Long getCacheMaxNumEntries() {
        return cacheMaxNumEntries;
    }

    public void setCacheMaxNumEntries(Long cacheMaxNumEntries) {
        this.cacheMaxNumEntries = cacheMaxNumEntries;
    }

    public Boolean getNoPatterns() {
        return noPatterns;
    }

    public void setNoPatterns(Boolean noPatterns) {
        this.noPatterns = noPatterns;
    }

    public Boolean getNoPatternTrick() {
        return noPatternTrick;
    }

    public void setNoPatternTrick(Boolean noPatternTrick) {
        this.noPatternTrick = noPatternTrick;
    }

    public Boolean getText() {
        return text;
    }

    public void setText(Boolean text) {
        this.text = text;
    }

    public Boolean getOnlyPsoAndPosPermutations() {
        return onlyPsoAndPosPermutations;
    }

    public void setOnlyPsoAndPosPermutations(Boolean onlyPsoAndPosPermutations) {
        this.onlyPsoAndPosPermutations = onlyPsoAndPosPermutations;
    }

    public String getDefaultQueryTimeout() {
        return defaultQueryTimeout;
    }

    public void setDefaultQueryTimeout(String defaultQueryTimeout) {
        this.defaultQueryTimeout = defaultQueryTimeout;
    }

    public Long getServiceMaxValueRows() {
        return serviceMaxValueRows;
    }

    public void setServiceMaxValueRows(Long serviceMaxValueRows) {
        this.serviceMaxValueRows = serviceMaxValueRows;
    }

    public Boolean getThrowOnUnboundVariables() {
        return throwOnUnboundVariables;
    }

    public void setThrowOnUnboundVariables(Boolean throwOnUnboundVariables) {
        this.throwOnUnboundVariables = throwOnUnboundVariables;
    }

    public void copyInto(QleverConfRun dest, boolean copyNulls) {
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
