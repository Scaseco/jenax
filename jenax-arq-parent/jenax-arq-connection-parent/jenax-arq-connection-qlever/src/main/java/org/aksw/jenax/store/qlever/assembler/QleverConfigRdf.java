package org.aksw.jenax.store.qlever.assembler;

import java.util.Optional;

import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import jenax.engine.qlever.docker.QleverConfig;

public class QleverConfigRdf
    extends ResourceImpl
    implements QleverConfig
{
    public QleverConfigRdf(Node n, EnhGraph m) {
        super(n, m);
    }

    protected Optional<Statement> tryGetStmt(Property p) {
        return Optional.ofNullable(getProperty(p));
    }

    // @Override
    public String getLocation() {
        return ResourceUtils.getLiteralPropertyValue(this, QleverAssemblerVocab.location, String.class);
    }

    // @Override
    public void setLocation(String location) {
        ResourceUtils.setLiteralProperty(this, QleverAssemblerVocab.location, location);
    }

    @Override
    public String getIndexBaseName() {
        return ResourceUtils.getLiteralPropertyValue(this, QleverAssemblerVocab.indexName, String.class);
    }

    @Override
    public void setIndexBaseName(String indexName) {
        ResourceUtils.setLiteralProperty(this, QleverAssemblerVocab.indexName, indexName);
    }

    @Override
    public Integer getPort() {
        return ResourceUtils.getLiteralPropertyValue(this, QleverAssemblerVocab.port, Integer.class);
    }

    @Override
    public void setPort(Integer port) {
        ResourceUtils.setLiteralProperty(this, QleverAssemblerVocab.port, port);
    }

    @Override
    public String getAccessToken() {
        return ResourceUtils.getLiteralPropertyValue(this, QleverAssemblerVocab.accessToken, String.class);
    }

    @Override
    public void setAccessToken(String accessToken) {
        ResourceUtils.setLiteralProperty(this, QleverAssemblerVocab.accessToken, accessToken);
    }

    @Override
    public Integer getNumSimultaneousQueries() {
        return ResourceUtils.getLiteralPropertyValue(this, QleverAssemblerVocab.numSimultaneousQueries, Integer.class);
    }

    @Override
    public void setNumSimultaneousQueries(Integer numSimultaneousQueries) {
        ResourceUtils.setLiteralProperty(this, QleverAssemblerVocab.numSimultaneousQueries, numSimultaneousQueries);
    }

    @Override
    public String getMemoryMaxSize() {
        return ResourceUtils.getLiteralPropertyValue(this, QleverAssemblerVocab.memoryMaxSize, String.class);
    }

    @Override
    public void setMemoryMaxSize(String memoryMaxSize) {
        ResourceUtils.setLiteralProperty(this, QleverAssemblerVocab.memoryMaxSize, memoryMaxSize);
    }

    @Override
    public String getCacheMaxSize() {
        return ResourceUtils.getLiteralPropertyValue(this, QleverAssemblerVocab.cacheMaxSize, String.class);
    }

    @Override
    public void setCacheMaxSize(String cacheMaxSize) {
        ResourceUtils.setLiteralProperty(this, QleverAssemblerVocab.cacheMaxSize, cacheMaxSize);
    }

    @Override
    public String getCacheMaxSizeSingleEntry() {
        return ResourceUtils.getLiteralPropertyValue(this, QleverAssemblerVocab.cacheMaxSizeSingleEntry, String.class);
    }

    @Override
    public void setCacheMaxSizeSingleEntry(String cacheMaxSizeSingleEntry) {
        ResourceUtils.setLiteralProperty(this, QleverAssemblerVocab.cacheMaxSizeSingleEntry, cacheMaxSizeSingleEntry);
    }

    @Override
    public String getLazyResultMaxCacheSize() {
        return ResourceUtils.getLiteralPropertyValue(this, QleverAssemblerVocab.lazyResultMaxCacheSize, String.class);
    }

    @Override
    public void setLazyResultMaxCacheSize(String lazyResultMaxCacheSize) {
        ResourceUtils.setLiteralProperty(this, QleverAssemblerVocab.lazyResultMaxCacheSize, lazyResultMaxCacheSize);
    }

    @Override
    public Long getCacheMaxNumEntries() {
        return ResourceUtils.getLiteralPropertyValue(this, QleverAssemblerVocab.cacheMaxNumEntries, Long.class);
    }

    @Override
    public void setCacheMaxNumEntries(Long cacheMaxNumEntries) {
        ResourceUtils.setLiteralProperty(this, QleverAssemblerVocab.cacheMaxNumEntries, cacheMaxNumEntries);
    }

    @Override
    public Boolean getNoPatterns() {
        return ResourceUtils.getLiteralPropertyValue(this, QleverAssemblerVocab.noPatterns, Boolean.class);
    }

    @Override
    public void setNoPatterns(Boolean noPatterns) {
        ResourceUtils.setLiteralProperty(this, QleverAssemblerVocab.noPatterns, noPatterns);
    }

    @Override
    public Boolean getNoPatternTrick() {
        return ResourceUtils.getLiteralPropertyValue(this, QleverAssemblerVocab.noPatternTrick, Boolean.class);
    }

    @Override
    public void setNoPatternTrick(Boolean noPatternTrick) {
        ResourceUtils.setLiteralProperty(this, QleverAssemblerVocab.noPatternTrick, noPatternTrick);
    }

    @Override
    public Boolean getText() {
        return ResourceUtils.getLiteralPropertyValue(this, QleverAssemblerVocab.text, Boolean.class);
    }

    @Override
    public void setText(Boolean text) {
        ResourceUtils.setLiteralProperty(this, QleverAssemblerVocab.text, text);
    }

    @Override
    public Boolean getOnlyPsoAndPosPermutations() {
        return ResourceUtils.getLiteralPropertyValue(this, QleverAssemblerVocab.onlyPsoAndPosPermutations, Boolean.class);
    }

    @Override
    public void setOnlyPsoAndPosPermutations(Boolean onlyPsoAndPosPermutations) {
        ResourceUtils.setLiteralProperty(this, QleverAssemblerVocab.onlyPsoAndPosPermutations, onlyPsoAndPosPermutations);
    }

    @Override
    public String getDefaultQueryTimeout() {
        return ResourceUtils.getLiteralPropertyValue(this, QleverAssemblerVocab.defaultQueryTimeout, String.class);
    }

    @Override
    public void setDefaultQueryTimeout(String defaultQueryTimeout) {
        ResourceUtils.setLiteralProperty(this, QleverAssemblerVocab.defaultQueryTimeout, defaultQueryTimeout);
    }

    @Override
    public Long getServiceMaxValueRows() {
        return ResourceUtils.getLiteralPropertyValue(this, QleverAssemblerVocab.serviceMaxValueRows, Long.class);
    }

    @Override
    public void setServiceMaxValueRows(Long serviceMaxValueRows) {
        ResourceUtils.setLiteralProperty(this, QleverAssemblerVocab.serviceMaxValueRows, serviceMaxValueRows);
    }

    @Override
    public Boolean getThrowOnUnboundVariables() {
        return ResourceUtils.getLiteralPropertyValue(this, QleverAssemblerVocab.throwOnUnboundVariables, Boolean.class);
    }

    @Override
    public void setThrowOnUnboundVariables(Boolean throwOnUnboundVariables) {
        ResourceUtils.setLiteralProperty(this, QleverAssemblerVocab.throwOnUnboundVariables, throwOnUnboundVariables);
    }
}
