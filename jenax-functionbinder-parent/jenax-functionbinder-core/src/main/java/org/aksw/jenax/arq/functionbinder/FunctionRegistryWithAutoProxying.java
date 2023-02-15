package org.aksw.jenax.arq.functionbinder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jenax.arq.util.security.ArqSecurity;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.Function;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.function.FunctionFactory;
import org.apache.jena.sparql.function.FunctionRegistry;

/**
 * This is a subclass of Jena's {@link FunctionRegistry} that adds the feature of
 * on-demand binding of SPARQL functions to Java methods using proxy generation.
 *
 * Unfortunately {@link FunctionRegistry} as of 4.8.0 does not have an extension point for
 * this kind of custom load strategies.
 */
public class FunctionRegistryWithAutoProxying
    extends FunctionRegistry
{
    @Override
    public FunctionFactory get(String uri) {
        FunctionFactory result = null;

        if (!isRegistered(uri)) {
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
                        // Nothing to do here
                    }

                    if (cls != null) {
                        List<FunctionAdapter> methods = Arrays.asList(cls.getMethods()).stream()
                            .filter(m -> m.getName().equals(methodName))
                            .flatMap(method -> {
                                Stream<FunctionAdapter> r;
                                try {
                                    FunctionAdapter adapter = generator.wrap(method);
                                    r = Stream.of(adapter);
                                } catch (Exception e) {
                                    // Ignore whatever we couldn't proxy
                                    r = Stream.empty();
                                }
                                return r;
                            })
                            .collect(Collectors.toList());
                        Function fn  = methods.size() == 1
                                ? methods.iterator().next()
                                : new FunctionMultiAdapter(methods);

                        result = anyUri -> new FunctionWrapperBase(fn) {
                            @Override
                            public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env) {
                                ArqSecurity.requireFileAccess(env.getContext());
                                return super.exec(binding, args, uri, env);
                            }
                        };

                        super.put(uri, result);
                    }
                }
            }
        }

        if (result == null) {
            result = super.get(uri);
        }

        return result;
    }
}
