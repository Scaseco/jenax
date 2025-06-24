package org.aksw.jenax.graphql.sparql.v2.exec.api.low;

// Adapting the writer from within the API feels bad, its better to have a high level API as a facade with the default adapters
//public class GraphQlFieldExecWrapper<K, KO, VO>
//    implements GraphQlFieldExec<K>
//{
//    protected GraphQlFieldExec<K> base;
//    protected Function<ObjectNotationWriter<KO, VO>, ObjectNotationWriter<K, Node>> adapter;
//
//    public GraphQlFieldExecWrapper(GraphQlFieldExec<K> base, Function<ObjectNotationWriter<KO, VO>, ObjectNotationWriter<K, Node>> adapter) {
//        super();
//        this.base = Objects.requireNonNull(base);
//        this.adapter = Objects.requireNonNull(adapter);
//    }
//
//    @Override
//    public boolean isSingle() {
//        return base.isSingle();
//    }
//
//    // @Override
//    public boolean sendNextItemToAdaptedWriter(ObjectNotationWriter<KO, VO> writer) throws IOException {
//        ObjectNotationWriter<K, Node> adaptedWriter = adapter.apply(writer);
//        boolean result = base.sendNextItemToWriter(adaptedWriter);
//        return result;
//    }
//
//    @Override
//    public void abort() {
//        base.abort();
//    }
//
//    @Override
//    public void close() {
//        base.close();
//    }
//
//    @Override
//    public boolean sendNextItemToWriter(ObjectNotationWriter<K, Node> writer) throws IOException {
//        return base.sendNextItemToWriter(writer);
//    }
//}
