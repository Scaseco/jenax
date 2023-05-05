package org.aksw.jenax.arq.functionbinder;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jenax.reprogen.shared.AnnotationUtils;
import org.apache.jena.sparql.function.Function;
import org.apache.jena.sparql.function.FunctionFactory;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class to create Jena bindings for Java functions
 * and to register them at Jena's default FunctionRegistry.
 *
 * @author raven
 *
 */
public class FunctionBinder {
    private static final Logger logger = LoggerFactory.getLogger(FunctionBinder.class);

    protected FunctionGenerator functionGenerator;
    protected FunctionRegistry functionRegistry;

    public FunctionBinder() {
        this(new FunctionGenerator(), FunctionRegistry.get());
    }

    public FunctionBinder(FunctionRegistry functionRegistry) {
        this(new FunctionGenerator(), functionRegistry);
    }

    public FunctionBinder(FunctionGenerator functionGenerator) {
        this(functionGenerator, FunctionRegistry.get());
    }

    public FunctionBinder(FunctionGenerator functionGenerator, FunctionRegistry functionRegistry) {
        super();
        this.functionGenerator = functionGenerator;
        this.functionRegistry = functionRegistry;
    }

    public FunctionGenerator getFunctionGenerator() {
        return functionGenerator;
    }

    public void register(String uri, Method method) {
        register(true, uri, method);
    }

    public void register(String uri, Method method, Object invocationTarget) {
        register(true, uri, method, invocationTarget);
    }

    public void register(Method method) {
        register(true, method);
    }

    public void register(Method method, Object invocationTarget) {
        register(true, method, invocationTarget);
    }

    public void registerAll(Class<?> clz) {
        registerAll(true, clz, null);
    }

    public void registerAll(Class<?> clz, Object invocationTarget) {
        registerAll(true, clz, invocationTarget);
    }




    /** Convenience method to register a function at Jena's default registry */
    public void register(boolean lazy, String functionIri, Method method) {
        register(functionIri, method, null);
    }

    /** Register a method with multiple aliases */
    public void register(boolean lazy, List<String> functionIris, Method method, Object invocationTarget) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Auto-binding SPARQL function(s) %s to %s (invocationTarget: %s)", functionIris, method, invocationTarget));
        }

        // Stopwatch sw = Stopwatch.createStarted();
        FunctionFactory factory = factory(lazy, method, invocationTarget);
        for (String iri : functionIris) {
            functionRegistry.put(iri, factory);
        }
        // logger.debug(String.format("Auto-binding SPARQL function %s to %s (invocationTarget: %s) tookn %.2f s", uri, method, invocationTarget, sw.elapsed(TimeUnit.SECONDS) * 0.001f));
    }

    public void register(boolean lazy, String functionIri, Method method, Object invocationTarget) {
        register(lazy, Collections.singletonList(functionIri), method, invocationTarget);
        // logger.debug(String.format("Auto-binding SPARQL function %s to %s (invocationTarget: %s) tookn %.2f s", uri, method, invocationTarget, sw.elapsed(TimeUnit.SECONDS) * 0.001f));
    }

    public void register(boolean lazy, Method method) {
        register(lazy, method, null);
    }


    /** Convenience method to register a function at Jena's default registry */
    public void register(boolean lazy, Method method, Object invocationTarget) {
        List<String> iris = AnnotationUtils.deriveIrisFromMethod(method, DefaultPrefixes.get());

        if (iris.isEmpty()) {
            throw new RuntimeException("No @Iri or @IriNs annotation present on method");
        }

        register(lazy, iris, method, invocationTarget);
    }


    /** Convenience method that hides checked exceptions from Class.getMethod */
    public void register(String iri, Class<?> clazz, String methodName, Class<?> ... paramTypes) {
        try {
            register(iri, clazz.getMethod(methodName, paramTypes));
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Register all static methods with @Iri annotations
     *
     */
    public void registerAll(boolean lazy, Class<?> clz) {
        registerAll(lazy, clz, null);
    }

    /**
     * Register all methods of the class with the given invocationTarget.
     * If the invocation target is null then all static methods will be registered.
     * Otherwise, if the invocation target is non-null then all non-static methods will be registered.
     *
     * @param clz
     * @param invocationTarget
     */
    public void registerAll(boolean lazy, Class<?> clz, Object invocationTarget) {
        for (Method method : clz.getMethods()) {
            List<String> iris = AnnotationUtils.deriveIrisFromMethod(method, DefaultPrefixes.get());

            if (!iris.isEmpty()) {
                boolean isStatic = Modifier.isStatic(method.getModifiers());

                // If the invocation target is null then only register static methods
                // otherwise only register only non-static methods
                if ((invocationTarget == null && isStatic) || (invocationTarget != null && !isStatic)) {
                    register(lazy, iris, method, invocationTarget);
                }

            }
        }
    }



    public FunctionFactory factory(boolean lazy, Method method) {
        return factory(lazy, method, null);
    }

    public FunctionFactory factory(boolean lazy, Method method, Object invocationTarget) {
        FunctionFactory result;

        if (lazy) {
            result = iri -> functionGenerator.wrap(method, invocationTarget);
        } else {
            Function fn = functionGenerator.wrap(method, invocationTarget);
            return iri -> fn;
        }

        return result;
    }

    /** Lookup a function for a given method in the default registry. Uses {@link #getFunction(FunctionRegistry, Method)}. */
    public static Function getFunction(Method method) {
        return getFunction(FunctionRegistry.get(), method);
    }

    /**
     * Read a (static) method's @Iri annotation and use it to lookup a function in the given registry
     *
     * At present there is no direct mapping of methods to FuncionFactories (without having to read the @Iri annotation)
     */
    public static Function getFunction(FunctionRegistry registry, Method method) {
        List<String> iris = AnnotationUtils.deriveIrisFromMethod(method, DefaultPrefixes.get());
        Function result = null;
        for (String iri : iris) {
            FunctionFactory factory = registry.get(iri);
            if (factory != null) {
                result = factory.create(iri);
                break;
            }
        }
        return result;
    }

}
