package org.aksw.jenax.dataaccess.sparql.link.update;

// Use LinkSparqlUpdateWrapperBase and implement newUpdate()

//public class LinkSparqlUpdateOverBuilder
//    implements LinkSparqlUpdateBase
//{
//    protected Supplier<UpdateExecBuilder> updateExecBuilderFactory;
//    protected Transactional transactional;
//
//    public LinkSparqlUpdateOverBuilder(Supplier<UpdateExecBuilder> updateExecBuilderFactory) {
//        this(updateExecBuilderFactory, new TransactionalNull());
//    }
//
//    public LinkSparqlUpdateOverBuilder(Supplier<UpdateExecBuilder> updateExecBuilderFactory,
//            Transactional transactional) {
//        super();
//        this.updateExecBuilderFactory = updateExecBuilderFactory;
//        this.transactional = transactional;
//    }
//
//    @Override
//    public Transactional getDelegate() {
//        return transactional;
//    }
//
//    @Override
//    public void update(Update update) {
//        newUpdate().update(update).execute();
//    }
//
//    @Override
//    public void update(UpdateRequest update) {
//        newUpdate().update(update).execute();
//
//    }
//
//    @Override
//    public void update(String updateString) {
//        newUpdate().update(updateString).execute();
//
//    }
//
//    @Override
//    public UpdateExecBuilder newUpdate() {
//        return updateExecBuilderFactory.get();
//    }
//
//    @Override
//    public void close() {
//    }
//}
