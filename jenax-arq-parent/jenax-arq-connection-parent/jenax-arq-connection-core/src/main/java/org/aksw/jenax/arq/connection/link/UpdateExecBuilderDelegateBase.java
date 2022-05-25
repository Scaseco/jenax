package org.aksw.jenax.arq.connection.link;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;

public class UpdateExecBuilderDelegateBase
	implements UpdateExecBuilder
{
	protected UpdateExecBuilder delegate;

	public UpdateExecBuilderDelegateBase(UpdateExecBuilder delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public UpdateExecBuilder update(UpdateRequest request) {
		delegate = delegate.update(request);
		return this;
	}

	@Override
    public UpdateExecBuilder update(Update update) {
		delegate = delegate.update(update);
		return this;
    }

	@Override
    public UpdateExecBuilder update(String updateString) {
		delegate = delegate.update(updateString);
		return this;
	}

	@Override
    public UpdateExecBuilder set(Symbol symbol, Object value) {
		delegate = delegate.set(symbol, value);
		return this;
	}

	@Override
    public UpdateExecBuilder set(Symbol symbol, boolean value) {
		delegate = delegate.set(symbol, value);
		return this;
    }


	@Override
	public UpdateExecBuilder context(Context context) {
		delegate = delegate.context(context);
		return this;
	}

    @Override
    public UpdateExecBuilder substitution(Binding binding) {
    	delegate = delegate.substitution(binding);
    	return this;
    }

    @Override
    public UpdateExecBuilder substitution(Var var, Node value) {
    	delegate = delegate.substitution(var, value);
    	return this;
    }

    @Override
    public UpdateExecBuilder substitution(String var, Node value) {
    	delegate = delegate.substitution(var, value);
    	return this;
    }

    @Override
    public UpdateExec build() {
    	UpdateExec result = delegate.build();
    	return result;
    }

    @Override
    public void execute() {
        build().execute();
    }

}
