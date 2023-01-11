package org.aksw.jena_sparql_api.sparql.ext.sys;

import org.aksw.commons.collector.domain.Accumulator;
import org.aksw.jena_sparql_api.sparql.ext.geosparql.AccAdapterJena;
import org.aksw.jenax.arq.functionbinder.FunctionAdapter;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.pfunction.*;

import java.lang.reflect.Method;
import java.util.Iterator;

public abstract class PropertyFunctionFactoryListFunctionsBase implements PropertyFunctionFactory {

    @Override
    public PropertyFunction create(String s) {
        return new PropertyFunctionEval(PropFuncArgType.PF_ARG_EITHER, PropFuncArgType.PF_ARG_LIST) {

            @Override
            public QueryIterator execEvaluated(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {
                if (argObject.getArgListSize() > 0) {
                    throw new QueryBuildException("Did not expect arguments to "+predicate.getURI());
                }
                RegWrapper registry = getRegistry(execCxt);

                Iterator<Binding> iter = getFunctions(binding, argSubject, registry);

                return QueryIterPlainWrapper.create(iter, execCxt);
            }
        };
    }

    protected abstract RegWrapper getRegistry(ExecutionContext execCxt);

    protected Iterator<Binding> getFunctions(Binding binding, PropFuncArg argSubject, RegWrapper registry) {
        Node subjectArg = argSubject.isList() ? argSubject.getArg(0) : argSubject.getArg();
        Node javFnArg = argSubject.getArgListSize() >= 2 ? argSubject.getArg(1) : null;
        Node factoryArg = argSubject.getArgListSize() >= 3 ? argSubject.getArg(2) : null;
        String deprecatedFn = "http://jena.hpl.hp.com/ARQ";
        return Iter.asStream(registry.keys())
                .filter(s -> !s.startsWith(deprecatedFn))
                .map(s -> {
                            BindingBuilder bb = BindingBuilder.create(binding);
                            bb.add(Var.alloc(subjectArg), NodeFactory.createURI(s));
                            Node ci = registry.getFnInfo(s);
                            if (ci != null && javFnArg != null)
                                bb.add(Var.alloc(javFnArg), ci);
                            if (factoryArg != null)
                                bb.add(Var.alloc(factoryArg), NodeFactory.createURI("java:" + registry.get(s).getClass().getName()));
                            return bb.build();
                        }
                ).iterator();
    }

    protected abstract static class RegWrapper {

        public abstract Object get(String uri);

        public abstract Object getCreate(String uri);

        public abstract Iterator<String> keys();

        protected Node getFnInfo(String s) {
            Object ret = null;
            try {
                Object fn = this.getCreate(s);
                ret = fn.getClass().getName();
                if (fn instanceof FunctionAdapter) {
                    Method method = ((FunctionAdapter) fn).getMethod();

                    Object invocationTarget = ((FunctionAdapter) fn).getInvocationTarget();
                    ret = (invocationTarget != null ? invocationTarget.getClass().getName() + "<-" : "")
                            + method.getDeclaringClass().getName() + "::" + method.getName();
                } else if (fn instanceof AccAdapterJena) {
                    Accumulator<Binding, FunctionEnv, NodeValue> accDelegate = ((AccAdapterJena) fn).getAccDelegate();
                    ret = ret + "#" + accDelegate.getClass().getName();

                }
            } catch (RuntimeException e) {
                ARQ.getExecLogger().warn("Function <"+ s +"> : "+ e.getMessage()) ;
            }
            return ret == null ? null : NodeFactory.createURI("java:" + ret);
        }
    }
}
