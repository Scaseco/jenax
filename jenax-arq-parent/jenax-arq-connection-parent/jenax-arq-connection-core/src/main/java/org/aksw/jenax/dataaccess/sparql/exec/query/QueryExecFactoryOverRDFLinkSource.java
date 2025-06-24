package org.aksw.jenax.dataaccess.sparql.exec.query;

// FIXME Delete - the RDFLinkSource.query method directly serves as a QueryExecFactory
//public class QueryExecFactoryOverRDFLinkSource
//    implements QueryExecFactory
//{
//    protected RDFLinkSource linkSource;
//
//    public QueryExecFactoryOverRDFLinkSource(RDFLinkSource linkSource) {
//        super();
//        this.linkSource = Objects.requireNonNull(linkSource);
//    }
//
//    @Override
//    public QueryExec create(Query query) {
//        QueryExec result = linkSource.query(query);
//        return result;
//    }
//
//    @Override
//    public QueryExec create(String queryString) {
//        QueryExec result = linkSource.query(queryString);
//        return result;
//    }
//}
