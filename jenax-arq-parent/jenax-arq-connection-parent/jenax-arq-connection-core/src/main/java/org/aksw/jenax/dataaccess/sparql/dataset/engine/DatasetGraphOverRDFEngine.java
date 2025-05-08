package org.aksw.jenax.dataaccess.sparql.dataset.engine;

import java.util.Iterator;

import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.aksw.jenax.dataaccess.sparql.exec.query.FragmentExec;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecUtils;
import org.aksw.jenax.sparql.fragment.impl.ConceptUtils;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.sparql.core.DatasetGraphBaseFind;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.exec.QueryExec;

public class DatasetGraphOverRDFEngine
    extends DatasetGraphBaseFind
    // implements DatasetGraphWrapperView
{
    protected RDFEngine engine;
    protected PrefixMap prefixes = PrefixMapFactory.emptyPrefixMap();

    public DatasetGraphOverRDFEngine(RDFEngine engine) {
        super();
        this.engine = engine;
    }

    public static DatasetGraphOverRDFEngine of(RDFEngine engine) {
        return new DatasetGraphOverRDFEngine(engine);
    }

    public RDFEngine getEngine() {
        return engine;
    }

    protected QueryExec exec(Query query) {
        return engine.getLinkSource().query(query);
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return FragmentExec.execNode(this::exec, ConceptUtils.listAllGraphs);
    }

    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o) {
        Iterator<Triple> base = QueryExecUtils.findTriples(this::exec, s, p, o);
        Iterator<Quad> result = Iter.map(base, t -> Quad.create(Quad.defaultGraphIRI, t));
        return result;
    }

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o) {
        Iterator<Quad> result = QueryExecUtils.findQuads(this::exec, g, s, p, o);
        return result;
    }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o) {
        Iterator<Quad> result = QueryExecUtils.findQuads(this::exec, Node.ANY, s, p, o);
        return result;
    }

    @Override
    public Graph getDefaultGraph() {
        return GraphView.createDefaultGraph(this);
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return GraphView.createNamedGraph(this, graphNode);
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
