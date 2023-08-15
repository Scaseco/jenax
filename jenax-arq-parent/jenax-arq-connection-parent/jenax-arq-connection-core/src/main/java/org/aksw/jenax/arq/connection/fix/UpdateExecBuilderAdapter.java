package org.aksw.jenax.arq.connection.fix;

//public class UpdateExecBuilderAdapter
//    implements UpdateExecBuilder
//{
//    protected UpdateExecutionBuilder delegate;
//
//    protected UpdateExecBuilderAdapter(UpdateExecutionBuilder delegate) {
//        super();
//        this.delegate = delegate;
//    }
//
//    public static UpdateExecBuilderAdapter adapt(UpdateExecutionBuilder delegate) {
//        Objects.requireNonNull(delegate);
//        return new UpdateExecBuilderAdapter(delegate);
//    }
//
//    @Override
//    public UpdateExecBuilder update(UpdateRequest updateRequest) {
//        delegate = delegate.update(updateRequest);
//        return this;
//    }
//
//    @Override
//    public UpdateExecBuilder update(Update update) {
//        delegate = delegate.update(update);
//        return this;
//    }
//
//    @Override
//    public UpdateExecBuilder update(String updateRequestString) {
//        delegate = delegate.update(updateRequestString);
//        return this;
//    }
//
//    @Override
//    public UpdateExecBuilder set(Symbol symbol, Object value) {
//        delegate = delegate.set(symbol, value);
//        return this;
//    }
//
//    @Override
//    public UpdateExecBuilder set(Symbol symbol, boolean value) {
//        delegate = delegate.set(symbol, value);
//        return this;
//    }
//
//    @Override
//    public UpdateExecBuilder context(Context context) {
//        delegate = delegate.context(context);
//        return this;
//    }
//
//    @Override
//    public UpdateExecBuilder substitution(Binding binding) {
//
//        delegate = delegate.substitution(new ResultBinding(null, binding));
//        return this;
//    }
//
//    @Override
//    public UpdateExecBuilder substitution(String varName, Node value) {
//        delegate = delegate.substitution(varName, ModelUtils.convertGraphNodeToRDFNode(value));
//        return this;
//    }
//
//    @Override
//    public UpdateExecBuilder substitution(Var var, Node value) {
//        delegate = delegate.substitution(var.getName(), ModelUtils.convertGraphNodeToRDFNode(value));
//        return this;
//    }
//
//    @Override
//    public UpdateExec build() {
//        UpdateExecution updateExec = delegate.build();
//        UpdateExec result = UpdateExecAdapter.adapt(updateExec);
//        return result;
//    }
//}
