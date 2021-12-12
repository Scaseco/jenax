package org.aksw.jenax.arq.functionbinder;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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
    public void register(boolean lazy, String uri, Method method) {
        register(uri, method, null);
    }

    public void register(boolean lazy, String uri, Method method, Object invocationTarget) {
        logger.debug(String.format("Auto-binding SPARQL function %s to %s (invocationTarget: %s)", uri, method, invocationTarget));
        // Stopwatch sw = Stopwatch.createStarted();
        FunctionFactory factory = factory(lazy, method, invocationTarget);
        functionRegistry.put(uri, factory);
        // logger.debug(String.format("Auto-binding SPARQL function %s to %s (invocationTarget: %s) tookn %.2f s", uri, method, invocationTarget, sw.elapsed(TimeUnit.SECONDS) * 0.001f));
    }

    public void register(boolean lazy, Method method) {
        register(lazy, method, null);
    }


    /** Convenience method to register a function at Jena's default registry */
    public void register(boolean lazy, Method method, Object invocationTarget) {
        String iri = AnnotationUtils.deriveIriFromMethod(method, DefaultPrefixes.get());

        if (iri == null) {
            throw new RuntimeException("No @Iri or @IriNs annotation present on method");
        }

        register(lazy, iri, method);
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
            String iri = AnnotationUtils.deriveIriFromMethod(method, DefaultPrefixes.get());

            if (iri != null) {
                boolean isStatic = Modifier.isStatic(method.getModifiers());

                // If the invocation target is null then only register static methods
                // otherwise only register only non-static methods
                if ((invocationTarget == null && isStatic) || (invocationTarget != null && !isStatic)) {
                    register(lazy, iri, method, invocationTarget);
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


}
