package org.aksw.jenax.dataaccess.sparql.builder.exec.update;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;

public interface UpdateExecBuilderWrapper
    extends UpdateExecBuilder
{
    UpdateExecBuilder getDelegate();

    @Override
    default UpdateExecBuilder update(UpdateRequest request) {
        getDelegate().update(request);
        return this;
    }

    @Override
    default UpdateExecBuilder update(Update update) {
        getDelegate().update(update);
        return this;
    }

    @Override
    default UpdateExecBuilder update(String updateString) {
        getDelegate().update(updateString);
        return this;
    }

    @Override
    default UpdateExecBuilder parseCheck(boolean parseCheck) {
        getDelegate().parseCheck(parseCheck);
        return this;
    }

    @Override
    default UpdateExecBuilder set(Symbol symbol, Object value) {
        getDelegate().set(symbol, value);
        return this;
    }

    @Override
    default UpdateExecBuilder set(Symbol symbol, boolean value) {
        getDelegate().set(symbol, value);
        return this;
    }

    @Override
    default UpdateExecBuilder context(Context context) {
        getDelegate().context(context);
        return this;
    }

    @Override
    default UpdateExecBuilder substitution(Binding binding) {
        getDelegate().substitution(binding);
        return this;
    }

    @Override
    default UpdateExecBuilder substitution(Var var, Node value) {
        getDelegate().substitution(var, value);
        return this;
    }

    @Override
    default UpdateExecBuilder substitution(String var, Node value) {
        getDelegate().substitution(var, value);
        return this;
    }

    @Override
    default UpdateExec build() {
        UpdateExec result = getDelegate().build();
        return result;
    }

    @Override
    default void execute() {
        build().execute();
    }
}
