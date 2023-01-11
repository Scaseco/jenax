package org.aksw.jena_sparql_api.sparql.ext.sys;

import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

import java.util.Iterator;

public class PropertyFunctionFactoryListPropertyFunctions extends PropertyFunctionFactoryListFunctionsBase {

    @Override
    protected PropertyFunctionFactoryListFunctionsBase.RegWrapper getRegistry(ExecutionContext execCxt) {
        return new RegWrapperPropertyFunctions(execCxt);
    }

    private class RegWrapperPropertyFunctions extends RegWrapper {
        private final PropertyFunctionRegistry reg;
        public RegWrapperPropertyFunctions(ExecutionContext execCxt) {
            PropertyFunctionRegistry registry1 = PropertyFunctionRegistry.get(execCxt.getContext());
            PropertyFunctionRegistry registry = registry1 != null ? registry1 : PropertyFunctionRegistry.get();
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
