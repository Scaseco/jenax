package org.aksw.jena_sparql_api.sparql.ext.sys;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorConcat;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.aggregate.AccumulatorFactory;
import org.apache.jena.sparql.expr.aggregate.AggCustom;
import org.apache.jena.sparql.expr.aggregate.AggregateRegistry;
import org.apache.jena.sparql.expr.nodevalue.NodeValueBoolean;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.graph.NodeConst;
import org.apache.jena.sparql.pfunction.PropFuncArg;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;

public class PropertyFunctionFactoryListAggregateFunctions extends PropertyFunctionFactoryListFunctionsBase {

    @Override
    protected Iterator<Binding> getFunctions(Binding binding, PropFuncArg argSubject, RegWrapper unused) {
        Node subjectArg = argSubject.isList() ? argSubject.getArg(0) : argSubject.getArg();
        Node distinctArg = argSubject.getArgListSize() >= 2 ? argSubject.getArg(1) : null;
        Node javFnArg = argSubject.getArgListSize() >= 3 ? argSubject.getArg(2) : null;
        Node factoryArg = argSubject.getArgListSize() >= 4 ? argSubject.getArg(3) : null;
        String deprecatedFn = "http://jena.hpl.hp.com/ARQ";
        boolean dv[] = {true, false};
        IteratorConcat<Binding> ret = new IteratorConcat<Binding>();
        for (boolean distinct : dv) {
            RegWrapperAggregates registry = new RegWrapperAggregates(distinct);
            ret.add(Iter.asStream(registry.keys())
                    .filter(s -> !s.startsWith(deprecatedFn))
                    .map(s -> {
                                BindingBuilder bb = BindingBuilder.create(binding);
                                bb.add(Var.alloc(subjectArg), NodeFactory.createURI(s));
                                if (distinctArg != null)
                                    bb.add(Var.alloc(distinctArg), distinct ? NodeConst.TRUE : NodeConst.FALSE);
                                Node ci = registry.getFnInfo(s);
                                if (ci != null && javFnArg != null)
                                    bb.add(Var.alloc(javFnArg), ci);
                                if (factoryArg != null)
                                    bb.add(Var.alloc(factoryArg), NodeFactory.createURI("java:" + registry.get(s).getClass().getName()));
                                return bb.build();
                            }
                    ).iterator()
            );
        }
        return ret;
    }

    @Override
    protected RegWrapper getRegistry(ExecutionContext execCxt) {
        return null;
    }

    private class RegWrapperAggregates extends RegWrapper {
        private final HashMap<String, AccumulatorFactory> reg;
        private final boolean distinct;

        public RegWrapperAggregates(boolean distinct) {
            Field registry = null;
            try {
                registry = AggregateRegistry.class.getDeclaredField("registry");
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            registry.setAccessible(true);
            try {
                reg = (HashMap<String, AccumulatorFactory>) registry.get(null);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            this.distinct = distinct;
        }

        public Object get(String uri) {
            return reg.get(uri);
        }

        @Override
        public Object getCreate(String uri) {
            return reg.get(uri).createAccumulator(new AggCustom(uri, distinct, ExprList.emptyList), distinct);
        }

        public Iterator<String> keys() {
            return reg.keySet().iterator();
        }

    }
}
