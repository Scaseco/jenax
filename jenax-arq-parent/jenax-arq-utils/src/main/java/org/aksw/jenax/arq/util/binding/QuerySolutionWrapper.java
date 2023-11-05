package org.aksw.jenax.arq.util.binding;

import java.util.Iterator;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.QuerySolutionBase;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingLib;

public class QuerySolutionWrapper
    extends QuerySolutionBase
{
    protected QuerySolution delegate;

    public QuerySolutionWrapper(QuerySolution querySolution) {
        this.delegate = querySolution;
    }

    public static Binding asBinding(QuerySolution qs) {
        Binding result = qs instanceof QuerySolutionWrapper
                ? ((QuerySolutionWrapper)qs).asBinding()
                : BindingLib.asBinding(qs);
        return result;
    }

    public QuerySolution getDelegate() {
        return delegate;
    }

    public Binding asBinding() {
        return asBinding(delegate);
    }

    @Override
    protected RDFNode _get(String varName) {
        return delegate.get(varName);
    }

    @Override
    protected boolean _contains(String varName) {
        return delegate.contains(varName);
    }

    @Override
    public Iterator<String> varNames() {
        return delegate.varNames();
    }
}
