package org.aksw.jenax.arq.connection.fix;

/** Jena 4.3.0-SNAPSHOT did not implement the update methods - this class can be removed once RDFLinkAdapter is fully functional */
//public class RDFLinkAdapterFix
//    extends RDFLinkAdapter
//{
//    // Just why are the relevant attributes always private...
//    protected RDFConnection conn;
//
//    public RDFLinkAdapterFix(RDFConnection conn) {
//        super(conn);
//        this.conn = conn;
//    }
//
//    @Override
//    public QueryExec query(Query query) {
//        // super.query(query) incorrectly returns null on at least jena 4.3.0-4.3.2 if
//        // conn.query() returns an QueryExecutionAdapter due to incorrect unwrapping
//        QueryExecution qe = conn.query(query);
//
//        // QueryExecAdapter can only be legally created with QueryExec.adapt - which checks the type of the argument
//        // Pass in our custom wrapper type in order to prevent the incorrect unwrapping
//        QueryExecutionDecoratorBase<QueryExecution> wrapper = new QueryExecutionDecoratorBase<QueryExecution>(qe);
//
//        QueryExec result = QueryExecAdapterFix.adapt(wrapper);
//        return result;
//    }
//
//    @Override
//    public void update(UpdateRequest update) { conn.update(update); }
//
//    @Override
//    public void update(String update) { conn.update(update); }
//
//    public static RDFLink adapt(RDFConnection conn) {
//        if ( conn instanceof RDFConnectionAdapter )
//            return ((RDFConnectionAdapter)conn).getLink();
//
//        return new RDFLinkAdapterFix(conn);
//    }
//
//
//    @Override
//    public UpdateExecBuilder newUpdate() {
//        UpdateExecutionBuilder ueb = conn.newUpdate();
//        return UpdateExecBuilderAdapter.adapt(ueb);
//    }
//}

