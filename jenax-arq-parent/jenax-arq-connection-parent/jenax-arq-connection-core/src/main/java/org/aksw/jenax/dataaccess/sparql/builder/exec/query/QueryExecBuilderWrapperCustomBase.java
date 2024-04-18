package org.aksw.jenax.dataaccess.sparql.builder.exec.query;

//public class QueryExecBuilderWrapperCustomBase
//    extends QueryExecBuilderCustomBase<QueryExecBuilder>
//    implements QueryExecBuilderWrapper<QueryExecBuilder>
//{
//    protected QueryExecBuilder delegate;
//
//    public QueryExecBuilderWrapperCustomBase(QueryExecBuilder delegate) {
//        super(delegate.getContext());
//        this.delegate = delegate;
//    }
//
//    @Override
//    public QueryExecBuilder getDelegate() {
//        return delegate;
//    }
//
//
//    @Override
//    public QueryExecBuilder query(Query query) {
//    	getDelegate().query(query);
//    	return super.query(query);
//    }
//
//    @Override
//    public QueryExecBuilder query(String queryString) {
//        this.query = null;
//        this.querySyntax = null;
//        this.queryString = queryString;
//        return self();
//    }
//
//    @Override
//    public QueryExecBuilder query(String queryString, Syntax syntax) {
//        this.query = null;
//        this.queryString = queryString;
//        this.querySyntax = syntax;
//        return self();
//    }
//
//    @Override
//    public QueryExecBuilder set(Symbol symbol, Object value) {
//        getContext().set(symbol, value);
//        return self();
//    }
//
//    @Override
//    public QueryExecBuilder set(Symbol symbol, boolean value) {
//        getContext().set(symbol, value);
//        return self();
//    }
//
//    @Override
//    public QueryExecBuilder context(Context context) {
//        Context.mergeCopy(getContext(), context);
//        return self();
//    }
//
//    @Override
//    public QueryExecBuilder substitution(Binding binding) {
//        substitution.addAll(binding);
//        return self();
//    }
//
//    @Override
//    public QueryExecBuilder substitution(Var var, Node value) {
//        substitution.add(var, value);
//        return self();
//    }
//
//    @Override
//    public QueryExecBuilder timeout(long value, TimeUnit timeUnit) {
//        overallTimeout(value, timeUnit);
//        return self();
//    }
//}
