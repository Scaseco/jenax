package org.aksw.jenax.store.qlever.assembler;

import java.util.Optional;

import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import jenax.engine.qlever.docker.QleverConfApi;

public class QleverConfig
    extends ResourceImpl
    implements QleverConfApi
{
    public QleverConfig(Node n, EnhGraph m) {
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setNumSimultaneousQueries(Integer numSimultaneousQueries) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getMemoryMaxSize() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setMemoryMaxSize(String memoryMaxSize) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getCacheMaxSize() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setCacheMaxSize(String cacheMaxSize) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getCacheMaxSizeSingleEntry() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setCacheMaxSizeSingleEntry(String cacheMaxSizeSingleEntry) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getLazyResultMaxCacheSize() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setLazyResultMaxCacheSize(String lazyResultMaxCacheSize) {
        // TODO Auto-generated method stub

    }

    @Override
    public Long getCacheMaxNumEntries() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setCacheMaxNumEntries(Long cacheMaxNumEntries) {
        // TODO Auto-generated method stub

    }

    @Override
    public Boolean getNoPatterns() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setNoPatterns(Boolean noPatterns) {
        // TODO Auto-generated method stub

    }

    @Override
    public Boolean getNoPatternTrick() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setNoPatternTrick(Boolean noPatternTrick) {
        // TODO Auto-generated method stub

    }

    @Override
    public Boolean getText() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setText(Boolean text) {
        // TODO Auto-generated method stub

    }

    @Override
    public Boolean getOnlyPsoAndPosPermutations() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setOnlyPsoAndPosPermutations(Boolean onlyPsoAndPosPermutations) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getDefaultQueryTimeout() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDefaultQueryTimeout(String defaultQueryTimeout) {
        // TODO Auto-generated method stub

    }

    @Override
    public Long getServiceMaxValueRows() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setServiceMaxValueRows(Long serviceMaxValueRows) {
        // TODO Auto-generated method stub

    }

    @Override
    public Boolean getThrowOnUnboundVariables() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setThrowOnUnboundVariables(Boolean throwOnUnboundVariables) {
        // TODO Auto-generated method stub

    }

}
