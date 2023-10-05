package org.aksw.jenax.dataaccess.sparql.link.common;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdflink.LinkDatasetGraph;
import org.apache.jena.rdflink.LinkSparqlUpdate;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.DatasetGraph;

// RdfSource loadFile(X)
//
//public class LinkDatasetGraphViaUpdate
//    implements LinkDatasetGraph
//{
//    protected StreamRDF sink;
//
//    public LinkDatasetGraphViaUpdate(StreamRDF sink) {
//
//    }
//
//    public static LinkDatasetGraph create(LinkSparqlUpdate link) {
//        // Set up the sink
//        StreamRDF sink = StreamRDFToUpdateRequest.createWithTrie(100, null, link::update);
//    }
//
//
//    @Override
//    public Graph get(Node graphName) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public Graph get() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public DatasetGraph getDataset() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public void begin(TxnType type) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void begin(ReadWrite readWrite) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public boolean promote(Promote mode) {
//        // TODO Auto-generated method stub
//        return false;
//    }
//
//    @Override
//    public void commit() {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void abort() {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void end() {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public ReadWrite transactionMode() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public TxnType transactionType() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public boolean isInTransaction() {
//        // TODO Auto-generated method stub
//        return false;
//    }
//
//    @Override
//    public void load(Node graphName, String file) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void load(String file) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void load(Node graphName, Graph graph) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void load(Graph graph) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void put(Node graphName, String file) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void put(String file) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void put(Node graphName, Graph graph) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void put(Graph graph) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void delete(Node graphName) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void delete() {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void loadDataset(String file) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void loadDataset(DatasetGraph dataset) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void putDataset(String file) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void putDataset(DatasetGraph dataset) {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public void clearDataset() {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public boolean isClosed() {
//        // TODO Auto-generated method stub
//        return false;
//    }
//
//    @Override
//    public void close() {
//        // TODO Auto-generated method stub
//
//    }
//
//}
