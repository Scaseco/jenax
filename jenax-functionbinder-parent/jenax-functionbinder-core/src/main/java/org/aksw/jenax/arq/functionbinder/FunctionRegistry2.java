package org.aksw.jenax.arq.functionbinder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.sparql.function.Function;
import org.apache.jena.sparql.function.FunctionFactory;
import org.apache.jena.sparql.function.FunctionRegistry;

public class FunctionRegistry2
    extends FunctionRegistry
{
    @Override
    public FunctionFactory get(String uri) {
        FunctionFactory result = null;

        // TODO If the uri is a java method reference then we need to return
        // a special function (similar to FunctionAdapter) that resolves the actual method
        // based on the argument types.
        String prefix = "java:";
        if (uri.startsWith(prefix)) {
            // fully qualified method name my.package.MyClass#myMethod
            int split = uri.lastIndexOf("#");
            if (split >= 0) {
                String fqcn = uri.substring(prefix.length(), split);
                String methodName = uri.substring(split + 1);

                FunctionBinder binder = FunctionBinders.getDefaultFunctionBinder();
                FunctionGenerator generator = binder.getFunctionGenerator();

                Class<?> cls = null;
                try {
                    cls = Class.forName(fqcn);
                } catch (ClassNotFoundException e) {

                }

                if (cls != null) {
                    List<FunctionAdapter> methods = Arrays.asList(cls.getMethods()).stream()
                        .filter(m -> m.getName().equals(methodName))
                        .map(method -> {
                            FunctionAdapter adapter = generator.wrap(method);
                            return adapter;
                        })
                        .collect(Collectors.toList());
                    Function fn  = methods.size() == 1
                            ? methods.iterator().next()
                            : new FunctionMultiAdapter(methods);

                    result = anyUri -> fn;
                }
            }
        }

        if (result == null) {
            result = super.get(uri);
        }

        return result;
    }
}
