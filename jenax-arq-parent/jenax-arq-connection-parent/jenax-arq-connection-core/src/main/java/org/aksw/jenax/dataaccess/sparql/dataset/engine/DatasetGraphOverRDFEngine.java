package org.aksw.jenax.dataaccess.sparql.dataset.engine;

import org.aksw.jenax.dataaccess.sparql.dataset.arq.DsgSparqlExecutor;
import org.aksw.jenax.dataaccess.sparql.dataset.rdflink.DatasetGraphOverRDFLink;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;

public class DatasetGraphOverRDFEngine
    extends DatasetGraphOverRDFLink
    // implements DatasetGraphWrapperView
{
    protected RDFEngine engine;
    protected PrefixMap prefixes = PrefixMapFactory.emptyPrefixMap();

    public DatasetGraphOverRDFEngine(RDFEngine engine) {
        super(DsgSparqlExecutor.DEFAULT, true, true);
        this.engine = engine;
    }

    public static DatasetGraphOverRDFEngine of(RDFEngine engine) {
        return new DatasetGraphOverRDFEngine(engine);
    }

    public RDFEngine getEngine() {
        return engine;
    }

    @Override
    public RDFLink newLink() {
        return engine.getLinkSource().newLink();
    }

//    protected QueryExec exec(Query query) {
//        return engine.getLinkSource().query(query);
//    }
//
//    @Override
//    public Iterator<Node> listGraphNodes() {
//        return FragmentExec.execNode(this::exec, ConceptUtils.listAllGraphs);
//    }
//
//    @Override
//    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o) {
//        Iterator<Triple> base = QueryExecUtils.findTriples(this::exec, s, p, o);
//        Iterator<Quad> result = Iter.map(base, t -> Quad.create(Quad.defaultGraphIRI, t));
//        return result;
//    }
//
//    @Override
//    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o) {
//        Iterator<Quad> result = QueryExecUtils.findQuads(this::exec, g, s, p, o);
//        return result;
//    }
//
//    @Override
//    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o) {
//        Iterator<Quad> result = QueryExecUtils.findQuads(this::exec, Node.ANY, s, p, o);
//        return result;
//    }
//
//    @Override
//    public Graph getDefaultGraph() {
//        return GraphView.createDefaultGraph(this);
//    }
//
//    @Override
//    public Graph getGraph(Node graphNode) {
//        return GraphView.createNamedGraph(this, graphNode);
//    }
//
//    @Override
//    public void addGraph(Node graphName, Graph graph) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public void removeGraph(Node graphName) {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public boolean supportsTransactions() {
//        return false;
//    }
//
//    @Override
//    public void abort() {
//    }
//
//    @Override
//    public void begin(ReadWrite arg0) {
//    }
//
//    @Override
//    public void commit() {
//    }
//
//    @Override
//    public void end() {
//    }
//
//    @Override
//    public boolean isInTransaction() {
//        return false;
//    }
//
//    @Override
//    public void begin(TxnType type) {
//    }
//
//    @Override
//    public boolean promote(Promote mode) {
//        return false;
//    }
//
//    @Override
//    public ReadWrite transactionMode() {
//        return ReadWrite.READ;
//    }
//
//    @Override
//    public TxnType transactionType() {
//        return null;
//    }
//
//    @Override
//    public PrefixMap prefixes() {
//        return prefixes;
//    }
//
//    @Override
//    protected Stream<Quad> streamInDftGraph(Node s, Node p, Node o) {
//        return Iter.asStream(findInDftGraph(s, p, o));
//    }
//
//    @Override
//    protected Stream<Quad> streamInSpecificNamedGraph(Node g, Node s, Node p, Node o) {
//        return Iter.asStream(findInSpecificNamedGraph(g, s, p, o));
//    }
//
//    @Override
//    protected Stream<Quad> streamInAnyNamedGraphs(Node s, Node p, Node o) {
//        return Iter.asStream(findInAnyNamedGraphs(s, p, o));
//    }
}
