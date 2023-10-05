package org.aksw.jena_sparql_api.core;

import java.util.Iterator;
import java.util.Set;

import org.aksw.commons.rx.lookup.MapService;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.lookup.ListServiceConcept;
import org.aksw.jenax.connectionless.SparqlService;
import org.aksw.jenax.dataaccess.sparql.execution.factory.query.QueryExecutionFactory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.sparql.core.DatasetGraphBaseFind;
import org.apache.jena.sparql.core.Quad;


@Deprecated /** Use DatasetGraphRdfDataSource */
public class DatasetGraphSparqlService
    extends DatasetGraphBaseFind
{
    //protected QueryExecutionFactory qef;
    protected SparqlService sparqlService;
    protected PrefixMap prefixes = PrefixMapFactory.emptyPrefixMap();

    public DatasetGraphSparqlService(SparqlService sparqlService) {
        this.sparqlService = sparqlService;
    }

    public SparqlService getSparqlService() {
        return sparqlService;
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
        MapService<Concept, Node, Node> ls = new ListServiceConcept(qef);
        Set<Node> nodes = ls.fetchData(ConceptUtils.listAllGraphs, null, null).keySet();
        return nodes.iterator();
    }

    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o) {
        QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
        Iterator<Quad> result = QueryExecutionUtils.findQuads(qef, Node.ANY, s, p, o);
        return result;
    }

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o) {
        QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
        Iterator<Quad> result = QueryExecutionUtils.findQuads(qef, g, s, p, o);
        return result;
    }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o) {
        QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
        Iterator<Quad> result = QueryExecutionUtils.findQuads(qef, Node.ANY, s, p, o);
        return result;
    }

    @Override
    public Graph getDefaultGraph() {
        return null;
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return null;
    }

    @Override
    public void addGraph(Node graphName, Graph graph) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeGraph(Node graphName) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean supportsTransactions() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void abort() {
        // TODO Auto-generated method stub

    }

    @Override
    public void begin(ReadWrite arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void commit() {
        // TODO Auto-generated method stub

    }

    @Override
    public void end() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isInTransaction() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void begin(TxnType type) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean promote(Promote mode) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public ReadWrite transactionMode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TxnType transactionType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PrefixMap prefixes() {
        return prefixes;
    }

}
