package org.aksw.jenax.dataaccess.sparql.builder.exec.update;

import java.util.concurrent.TimeUnit;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.Timeouts.TimeoutBuilderImpl;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ContextAccumulator;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

public abstract class UpdateExecBuilderCustomBase<T extends UpdateExecBuilder>
    implements UpdateExecBuilder
{
    protected UpdateRequest updateRequest;
    protected Update update;
    protected String updateString;
    protected boolean parseCheck = true;
    protected TimeoutBuilderImpl timeoutBuilder = new TimeoutBuilderImpl();
    protected BindingBuilder substitution = BindingFactory.builder();
    protected ContextAccumulator contextAccumulator;

    public UpdateExecBuilderCustomBase() {
        this(ContextAccumulator.newBuilder());
    }

    public UpdateExecBuilderCustomBase(ContextAccumulator context) {
        super();
        this.contextAccumulator = context;
    }

    public UpdateRequest getUpdateRequest() {
        return updateRequest;
    }

    public Update getUpdate() {
        return update;
    }

    public String getUpdateString() {
        return updateString;
    }

    public BindingBuilder getSubstitution() {
        return substitution;
    }

    public Context getContext() {
        return contextAccumulator.context();
    }

    /**
     * Returns the update request.
     * Each invocation parses it if is set as a string.
     * Returns null if no update was set.
     */
    public UpdateRequest getParsedUpdateRequest() {
        UpdateRequest result = updateRequest != null
            ? updateRequest
            : update != null
                ? new UpdateRequest(update)
                : updateString != null
                    ? UpdateFactory.create(updateString)
                    : null;
        return result;
    }

    @SuppressWarnings("unchecked")
    public T self() {
        return (T)this;
    }

    @Override
    public T update(UpdateRequest request) {
        this.updateRequest = request;
        this.update = null;
        this.updateString = null;
        return self();
    }

    @Override
    public T update(Update update) {
        this.updateRequest = null;
        this.update = update;
        this.updateString = null;
        return self();
    }

    @Override
    public T update(String updateString) {
        this.updateRequest = parseCheck ? UpdateFactory.create(updateString) : null;
        this.update = null;
        this.updateString = updateString;
        return self();
    }

    @Override
    public UpdateExecBuilder parseCheck(boolean parseCheck) {
        this.parseCheck = parseCheck;
        return self();
    }

    @Override
    public UpdateExecBuilder set(Symbol symbol, Object value) {
        contextAccumulator.set(symbol, value);
        return self();
    }

    @Override
    public UpdateExecBuilder set(Symbol symbol, boolean value) {
        contextAccumulator.set(symbol, value);
        return self();
    }

    @Override
    public UpdateExecBuilder context(Context context) {
        if (context != null) {
            for (Symbol key : context.keys()) {
                this.contextAccumulator.set(key, context.get(key));
            }
        }
        // this.contextAccumulator.putAll(context);
        return self();
    }

    @Override
    public UpdateExecBuilder timeout(long value, TimeUnit timeUnit) {
        timeoutBuilder.timeout(value, timeUnit);
        return self();
    }

    @Override
    public UpdateExecBuilder substitution(Binding binding) {
        this.substitution.addAll(binding);
        return self();
    }

    @Override
    public UpdateExecBuilder substitution(Var var, Node value) {
        this.substitution.add(var, value);
        return self();
    }

    public void applySettings(UpdateExecBuilder dst) {
        if (updateRequest != null) {
            dst.update(updateRequest);
        } else if (update != null) {
            dst.update(update);
        } else if (updateString != null) {
            dst.update(updateString);
        }

        Binding binding = substitution.build();
        if (!binding.isEmpty()) {
            dst.substitution(binding);
        }

        if (contextAccumulator != null) {
            dst.context(contextAccumulator.context());
        }
    }
}
