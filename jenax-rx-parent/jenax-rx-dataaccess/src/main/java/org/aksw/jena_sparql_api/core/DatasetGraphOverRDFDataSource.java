package org.aksw.jena_sparql_api.core;

// Replaced by DatasetGraphOverRDFEngine

//public class DatasetGraphOverRDFDataSource
//    extends DatasetGraphBaseFind
//{
//    protected RDFDataSource dataSource;
//    protected PrefixMap prefixes = PrefixMapFactory.emptyPrefixMap();
//
//    public DatasetGraphOverRDFDataSource(RDFDataSource dataSource) {
//        this.dataSource = dataSource;
//    }
//
//    public RDFDataSource getDataSource() {
//        return dataSource;
//    }
//
//    @Override
//    public Iterator<Node> listGraphNodes() {
//        QueryExecutionFactory qef = dataSource.asQef();
//        MapService<Fragment1, Node, Node> ls = new ListServiceConcept(qef);
//        Set<Node> nodes = ls.fetchData(ConceptUtils.listAllGraphs, null, null).keySet();
//        return nodes.iterator();
//    }
//
//    @Override
//    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o) {
//        QueryExecutionFactory qef = dataSource.asQef();
//        Iterator<Quad> result = QueryExecutionUtils.findQuads(qef, Node.ANY, s, p, o);
//        return result;
//    }
//
//    @Override
//    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o) {
//        QueryExecutionFactory qef = dataSource.asQef();
//        Iterator<Quad> result = QueryExecutionUtils.findQuads(qef, g, s, p, o);
//        return result;
//    }
//
//    @Override
//    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o) {
//        QueryExecutionFactory qef = dataSource.asQef();
//        Iterator<Quad> result = QueryExecutionUtils.findQuads(qef, Node.ANY, s, p, o);
//        return result;
//    }
//
//    @Override
//    public Graph getDefaultGraph() {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public Graph getGraph(Node graphNode) {
//        throw new UnsupportedOperationException();
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
//    /** TODO Cleanup: This method has a duplicate in QueryExecutionUtils */
//    public static Iterator<Quad> findQuads(QueryExecutionFactory qef, Node g, Node s, Node p, Node o) {
//        Quad quad = new Quad(g, s, p, o);
//        Query query = QueryGenerationUtils.createQueryQuad(new Quad(g, s, p, o));
//        BindingMapper<Quad> mapper = new BindingMapperQuad(quad);
//        Iterator<Quad> result = BindingMapperUtils.execMapped(qef, query, mapper);
//        return result;
//    }
//
//}
