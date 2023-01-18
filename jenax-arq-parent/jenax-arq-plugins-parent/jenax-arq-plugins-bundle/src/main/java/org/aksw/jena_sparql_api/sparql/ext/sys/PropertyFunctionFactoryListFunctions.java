package org.aksw.jena_sparql_api.sparql.ext.sys;

import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.function.FunctionRegistry;

import java.util.Iterator;

public class PropertyFunctionFactoryListFunctions extends PropertyFunctionFactoryListFunctionsBase {
    @Override
    protected PropertyFunctionFactoryListFunctionsBase.RegWrapper getRegistry(ExecutionContext execCxt) {
        return new RegWrapperFunctions(execCxt);
    }

    private class RegWrapperFunctions extends RegWrapper {
        private final FunctionRegistry reg;
        public RegWrapperFunctions(ExecutionContext execCxt) {
            FunctionRegistry registry1 = FunctionRegistry.get(execCxt.getContext());
            FunctionRegistry registry = registry1 != null ? registry1 : FunctionRegistry.get();
            this.reg = registry;
        }

        public Object get(String uri) {
            return reg.get(uri);
        }

        public Object getCreate(String uri) {
            return reg.get(uri).create(uri);
        }

        public Iterator<String> keys() {
            return reg.keys();
        }

    }
}
