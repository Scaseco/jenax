package org.aksw.jena_sparql_api.dataset.file;

import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.SystemARQ ;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphCollection;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.DatasetGraphFactory.GraphMaker ;
import org.apache.jena.sparql.core.DatasetGraphMap;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.graph.GraphUnionRead ;
import org.apache.jena.sparql.graph.GraphZero;

/** Implementation of a DatasetGraph as an extensible set of graphs where graphs are held by reference.
 *  Care is needed when manipulating their contents
 *  especially if they are also in another {@code DatasetGraph}.
 *  <p>
 *  See {@link DatasetGraphMap} for an implementation that copies graphs
 *  and so providing better isolation.
 *  <p>
 *  This class is best used for creating views
 *
 *  @see DatasetGraphMap
 */
public class DatasetGraphMapLink2 extends DatasetGraphCollection
{
    private final GraphMaker graphMaker ;
    private final Map<Node, Graph> graphs = new HashMap<>() ;

    private Graph defaultGraph ;
    private final Transactional txn;
    private final TxnDataset2Graph2 txnDsg2Graph;
    private static GraphMaker dftGraphMaker = DatasetGraphFactory.graphMakerMem;

    /**
     * Create a new {@code DatasetGraph} that copies the dataset structure of default
     * graph and named graph and links to the graphs of the original {@code DatasetGraph}.
     * Any new graphs needed are separate from the original dataset and created in-memory.
     */
    public static DatasetGraph cloneStructure(DatasetGraph dsg) {
        return cloneStructure(dsg, dftGraphMaker);
    }

    /**
     * Create a new {@code DatasetGraph} that copies the dataset structure of default
     * graph and named graph and links to the graphs of the original {@code DatasetGraph}
     * Any new graphs needed are separate from the original dataset and created according
     * to the {@link GraphMaker}.
     */
    public static DatasetGraph cloneStructure(DatasetGraph dsg, GraphMaker graphMaker) {
        DatasetGraphMapLink2 dsg2 = new DatasetGraphMapLink2((Graph)null, graphMaker);
        linkGraphs(dsg, dsg2);
        return dsg2;
    }

    private static void linkGraphs(DatasetGraph srcDsg, DatasetGraphMapLink2 dstDsg) {
        dstDsg.defaultGraph = srcDsg.getDefaultGraph();
        for ( Iterator<Node> names = srcDsg.listGraphNodes() ; names.hasNext() ; ) {
            Node gn = names.next() ;
            dstDsg.addGraph(gn, srcDsg.getGraph(gn)) ;
        }
    }

    /** A {@code DatasetGraph} that uses the given graph for the default graph
     *  and create in-memory graphs for named graphs as needed
     */
    public DatasetGraphMapLink2(Graph dftGraph) {
        this(dftGraph, dftGraphMaker);
    }

    // This is the root constructor.
    public DatasetGraphMapLink2(Graph dftGraph, GraphMaker graphMaker) {
        this.graphMaker = graphMaker;
        this.defaultGraph = dftGraph;
        txnDsg2Graph = new TxnDataset2Graph2(dftGraph);
        txn = txnDsg2Graph;
    }

    @Override
    public void commit() {
        if ( txnDsg2Graph == null )
            SystemARQ.sync(this);
        txn.commit() ;
    }

    @Override public void begin()                       { txn.begin(); }
    @Override public void begin(TxnType txnType)        { txn.begin(txnType); }
    @Override public void begin(ReadWrite mode)         { txn.begin(mode); }
    @Override public boolean promote(Promote txnType)   { return txn.promote(txnType); }
    //Above: commit()
    @Override public void abort()                       { txn.abort(); }
    @Override public boolean isInTransaction()          { return txn.isInTransaction(); }
    @Override public void end()                         { txn.end(); }
    @Override public ReadWrite transactionMode()        { return txn.transactionMode(); }
    @Override public TxnType transactionType()          { return txn.transactionType(); }
    @Override public boolean supportsTransactions()     { return true; }
    @Override public boolean supportsTransactionAbort() { return false; }
    // ----

    @Override
    public boolean containsGraph(Node graphNode) {
        if ( Quad.isDefaultGraph(graphNode) || Quad.isUnionGraph(graphNode) )
            return true;
        return graphs.containsKey(graphNode);
    }

    @Override
    public Graph getDefaultGraph() {
        return defaultGraph;
    }

    @Override
    public Graph getGraph(Node graphNode) {
        // Same as DatasetGraphMap.getGraph but we inherit differently.
        if ( Quad.isUnionGraph(graphNode) )
            return new GraphUnionRead(this) ;
        if ( Quad.isDefaultGraph(graphNode))
            return getDefaultGraph() ;
        // Not a special case.
        Graph g = graphs.get(graphNode);
        if ( g == null ) {
            g = getGraphCreate(graphNode);
            if ( g != null )
                addGraph(graphNode, g);
        }
        return g;
    }

    /**
     * Called from getGraph when a nonexistent graph is asked for.
     * Return null for "nothing created as a graph"
     */
    protected Graph getGraphCreate(Node graphNode) {
        return graphMaker.create(graphNode) ;
    }

    @Override
    public void addGraph(Node graphName, Graph graph) {
        if ( txnDsg2Graph != null )
            txnDsg2Graph.addGraph(graph);
        graphs.put(graphName, graph);
    }

    @Override
    public void removeGraph(Node graphName) {
        Graph g = graphs.remove(graphName);
        if ( g != null && txnDsg2Graph != null )
            txnDsg2Graph.removeGraph(g);
    }

    // @Override
    // TODO setDefaultGraph was removed in jena 5 - but we need to check whether any other methods needs to be updated to accomodate for possible changes in the semantics
    public void setDefaultGraph(Graph g) {
        if ( g == null )
            // Always have a default graph of some kind.
            g = GraphZero.instance();
        if ( txnDsg2Graph != null )
            txnDsg2Graph.addGraph(g);
        defaultGraph = g;
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return graphs.keySet().iterator();
    }

    @Override
    public long size() {
        return graphs.size();
    }

    @Override
    public void close() {
        defaultGraph.close();
        for ( Graph graph : graphs.values() )
            graph.close();
        super.close();
    }

    @Override
    public PrefixMap prefixes() {
        throw new UnsupportedOperationException();
    }
}
