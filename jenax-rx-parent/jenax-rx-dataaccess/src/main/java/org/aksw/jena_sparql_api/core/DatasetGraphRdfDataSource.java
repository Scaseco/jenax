package org.aksw.jena_sparql_api.core;

import java.util.Iterator;
import java.util.Set;

import org.aksw.commons.rx.lookup.MapService;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.lookup.ListServiceConcept;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.sparql.core.DatasetGraphBaseFind;
import org.apache.jena.sparql.core.Quad;


public class DatasetGraphRdfDataSource
    extends DatasetGraphBaseFind
{
    protected RdfDataSource dataSource;
    protected PrefixMap prefixes = PrefixMapFactory.emptyPrefixMap();

    public DatasetGraphRdfDataSource(RdfDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public RdfDataSource getDataSource() {
        return dataSource;
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        QueryExecutionFactory qef = dataSource.asQef();
        MapService<Concept, Node, Node> ls = new ListServiceConcept(qef);
        Set<Node> nodes = ls.fetchData(ConceptUtils.listAllGraphs, null, null).keySet();
        return nodes.iterator();
    }

    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o) {
        QueryExecutionFactory qef = dataSource.asQef();
        Iterator<Quad> result = QueryExecutionUtils.findQuads(qef, Node.ANY, s, p, o);
        return result;
    }

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o) {
        QueryExecutionFactory qef = dataSource.asQef();
        Iterator<Quad> result = QueryExecutionUtils.findQuads(qef, g, s, p, o);
        return result;
    }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o) {
        QueryExecutionFactory qef = dataSource.asQef();
        Iterator<Quad> result = QueryExecutionUtils.findQuads(qef, Node.ANY, s, p, o);
        return result;
    }

    @Override
    public Graph getDefaultGraph() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Graph getGraph(Node graphNode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addGraph(Node graphName, Graph graph) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeGraph(Node graphName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsTransactions() {
        return false;
    }

    @Override
    public void abort() {
    }

    @Override
    public void begin(ReadWrite arg0) {
    }

    @Override
    public void commit() {
    }

    @Override
    public void end() {
    }

    @Override
    public boolean isInTransaction() {
        return false;
    }

    @Override
    public void begin(TxnType type) {
    }

    @Override
    public boolean promote(Promote mode) {
        return false;
    }

    @Override
    public ReadWrite transactionMode() {
        return ReadWrite.READ;
    }

    @Override
    public TxnType transactionType() {
        return null;
    }

    @Override
    public PrefixMap prefixes() {
        return prefixes;
    }

}
