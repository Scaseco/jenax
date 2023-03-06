package org.aksw.jenax.reprogen.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.mapper.proxy.TypeDecider;
import org.aksw.jena_sparql_api.mapper.proxy.TypeDeciderImpl;
import org.aksw.jena_sparql_api.rdf.collections.RDFNodeMapperImpl;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.arq.util.implementation.ImplementationLazy;
import org.aksw.jenax.reprogen.util.ImplementationProxy;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.ext.com.google.common.reflect.ClassPath;
import org.apache.jena.ext.com.google.common.reflect.ClassPath.ClassInfo;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JenaPluginUtils {

    private static final Logger logger = LoggerFactory.getLogger(JenaPluginUtils.class);


    static {
        JenaSystem.init();
    }

    /**
     * The type decider can
     * <ul>
     * 	 <li>decide for a given RDFNode whether a certain class can act as a view for it</li>
     *   <li>for a given view write out those triples to an RDFNode such that the type decider
     *   will consider the original view as applicable</li>
     * </ul>
     *
     * If you get an exception on typeDecider such as java.lang.NullPointerException
     * ensure to call JenaSystem.init() before calling methods on this class
     */
    protected static TypeDeciderImpl typeDecider;



    public static <T extends RDFNode> T polymorphicCast(RDFNode rdfNode, Class<T> viewClass, TypeDecider typeDecider) {
        T result = RDFNodeMapperImpl.castRdfNode(rdfNode, viewClass, typeDecider, false, false);
        return result;
    }

    /**
     * Cast an RDFNode to a given view w.r.t. types registered in the global TypeDecider
     *
     * @param <T>
     * @param rdfNode
     * @param viewClass
     * @return
     */
    public static <T extends RDFNode> T polymorphicCast(RDFNode rdfNode, Class<T> viewClass) {
        TypeDecider typeDecider = getTypeDecider();
        T result = RDFNodeMapperImpl.castRdfNode(rdfNode, viewClass, typeDecider, false, false);
        return result;
    }

    /**
     * Cast an RDFNode to a given view w.r.t. types registered in the global TypeDecider
     *
     * @param <T>
     * @param rdfNode
     * @return
     */
    public static <T extends RDFNode> T polymorphicCast(RDFNode rdfNode) {
        TypeDecider typeDecider = getTypeDecider();
        Collection<Class<?>> candidateClasses = typeDecider.getApplicableTypes(rdfNode.asResource());

        Class<?> clz = IterableUtils.expectOneItem(candidateClasses);
        @SuppressWarnings("unchecked")
        T result = polymorphicCast(rdfNode, (Class<T>)clz, typeDecider);

        return result;
    }



    public static <T extends RDFNode> T inModel(T rdfNode, Class<T> viewClass, Model target) {
        RDFNode r = rdfNode.inModel(target);
        T result = polymorphicCast(r, viewClass);
        return result;
    }

    /**
     * Copy all triples of the given rdf node into the target model and return
     * the result of a polymorphic cast
     *
     * @param <T>
     * @param rdfNode
     * @param viewClass
     * @param target
     * @return
     */
    public static <T extends Resource> T copyInto(T rdfNode, Class<T> viewClass, Model target) {
        Model m = rdfNode.getModel();
        target.add(m);
        T result = inModel(rdfNode, viewClass, target);
        return result;
    }

    /**
     * Copy only the triples of the closure of the given rdf node into the target model and return
     * the result of a polymorphic cast
     *
     * @param <T>
     * @param rdfNode
     * @param viewClass
     * @param target
     * @return
     */
    public static <T extends RDFNode> T copyClosureInto(T rdfNode, Class<T> viewClass, Model target) {
        if(rdfNode.isResource()) {
            Resource r = rdfNode.asResource();
            Model closure = ResourceUtils.reachableClosure(r);
            target.add(closure);
        }

        T result = inModel(rdfNode, viewClass, target);
        return result;
    }

    public static <T extends RDFNode> T reachableClosure(T rdfNode, Class<T> viewClass) {
        Model target;
        if(rdfNode.isResource()) {
            Resource r = rdfNode.asResource();
            target = ResourceUtils.reachableClosure(r);
        } else {
            target = ModelFactory.createDefaultModel();
        }

        T result = inModel(rdfNode, viewClass, target);
        return result;
    }

//	public static <T extends Resource> T copyClosure(T rdfNode, Class<T> viewClass) {
//		Model m = ResourceUtils.reachableClosure(rdfNode);
//		T result = inModel(rdfNode, viewClass, m);
//		return result;
//	}

    public static TypeDecider getTypeDecider() {
        if(typeDecider == null) {
            synchronized(JenaPluginUtils.class) {
                if (typeDecider == null) {
                    typeDecider = new TypeDeciderImpl();
                }
            }
        }
        return typeDecider;
    }

    public static void scan(Class<?> prototypeClass) {
        String basePackage = prototypeClass.getPackage().getName();
        scan(basePackage, BuiltinPersonalities.model);
    }

    public static void scan(String basePackage) {
        scan(basePackage, BuiltinPersonalities.model);
    }

    public static void scan(String basePackage, Personality<RDFNode> p) {
        scan(basePackage, p, DefaultPrefixes.get());
    }

    public static void scan(String basePackage, Personality<RDFNode> p, PrefixMapping pm) {
        Set<ClassInfo> classInfos;
//        System.err.println("Scanning " + basePackage);
        try {
            classInfos = ClassPath.from(Thread.currentThread().getContextClassLoader()).getTopLevelClassesRecursive(basePackage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for(ClassInfo classInfo : classInfos) {
            Class<?> clazz = classInfo.load();

            registerResourceClass(true, clazz, p, () -> pm);
        }
    }

    @SafeVarargs
    public static void registerResourceClasses(Class<? extends Resource> ... classes) {
        registerResourceClasses(true, classes);
    }

    public static void registerResourceClasses(Iterable<Class<?>> classes) {
        registerResourceClasses(true, classes);
    }


    @SafeVarargs
    public static void registerResourceClasses(boolean lazy, Class<? extends Resource> ... classes) {
        registerResourceClasses(lazy, Arrays.asList(classes));
    }

    public static void registerResourceClasses(boolean lazy, Iterable<Class<?>> classes) {
        for(Class<?> clazz : classes) {
            registerResourceClass(lazy, clazz, BuiltinPersonalities.model, () -> DefaultPrefixes.get());
        }
    }

    public static void registerResourceClass(Class<? extends Resource> inter, Class<?> impl) {
        Personality<RDFNode> p = BuiltinPersonalities.model;

        if(Resource.class.isAssignableFrom(impl)) {
            boolean supportsProxying = supportsProxying(impl);
            if(supportsProxying) {
                @SuppressWarnings("unchecked")
                Class<? extends Resource> cls = (Class<? extends Resource>)impl;
                p.add(inter, createImplementation(cls, DefaultPrefixes.get()));
            }
        }
    }

    public static Implementation createImplementation(boolean lazy, Class<?> clazz, Supplier<PrefixMapping> pm) {
        Implementation result;

        TypeDecider typeDecider = getTypeDecider();
        ((TypeDeciderImpl)typeDecider).registerClasses(clazz);

        if (lazy) {
            // We don't clone the prefixes anymore for performance reasons

            // Better clone the prefix mapping as the provided may have changed
            // by the time we actually perform the init
            // PrefixMapping clone = new PrefixMappingImpl();
            // clone.setNsPrefixes(pm);
            result = new ImplementationLazy(() -> createImplementation(clazz, pm.get()), clazz);
        } else {
            result = createImplementation(clazz, pm.get());
        }

        return result;
    }

    public static Implementation createImplementation(Class<?> clazz, PrefixMapping pm) {
        @SuppressWarnings("unchecked")
        Class<? extends Resource> cls = (Class<? extends Resource>)clazz;

        TypeDecider typeDecider = getTypeDecider();

        logger.debug("Creating implementation for " + clazz);
        BiFunction<Node, EnhGraph, ? extends Resource> proxyFactory =
                MapperProxyUtils.createProxyFactory(cls, pm, typeDecider);


        BiFunction<Node, EnhGraph, ? extends Resource> proxyFactory2 = (n, m) -> {
            Resource tmp = new ResourceImpl(n, m);
            typeDecider.writeTypeTriples(tmp, cls);

            Resource r = proxyFactory.apply(n, m);
            return r;
        };

        Implementation result = new ImplementationProxy(proxyFactory2);
        return result;
    }

//    public static void registerResourceClass(Class<?> clazz, Personality<RDFNode> p, PrefixMapping pm) {
//        registerResourceClass(clazz, p, pm, false);
//    }

    public static void registerResourceClass(boolean lazy, Class<?> clazz, Personality<RDFNode> p, Supplier<PrefixMapping> pm) {

        if (Resource.class.isAssignableFrom(clazz)) {
            boolean supportsProxying = supportsProxying(clazz);
            if (supportsProxying) {
                ResourceView resourceView = clazz.getAnnotation(ResourceView.class);
                Class<?>[] rawSuperTypes = resourceView == null
                        ? null
                        : resourceView.value();

                // If ResourceView is used without arguments, use the annotated type itself
                Class<?>[] superTypes = rawSuperTypes == null || rawSuperTypes.length == 0
                        ? new Class<?>[] {clazz}
                        : rawSuperTypes;

                List<Class<?>> effectiveTypes = new ArrayList<>(Arrays.asList(superTypes));
                //effectiveTypes.add(clazz);

                Implementation impl = createImplementation(lazy, clazz, pm);

                for(Class<?> type : effectiveTypes) {
                    if(!type.isAssignableFrom(clazz)) {
                        logger.warn("Not a super type: Cannot register implementation for " + clazz + " with specified type " + type);
                    } else {
                        @SuppressWarnings("unchecked")
                        Class<? extends Resource> cls = (Class<? extends Resource>)type;

                        logger.debug("Registering ResourceView from " + clazz);
                        p.add(cls, impl);
                    }
                }
            }
        }
    }

    public static boolean supportsProxying(Class<?> clazz) {
        boolean result = false;
        //int mods = clazz.getModifiers();
        //if(Modifier.isInterface(mods) || !Modifier.isAbstract(mods)) {
            // Check if the class is annotated by @ResourceView
            result = clazz.getAnnotation(ResourceView.class) != null;

            // Check if there ary any @Iri annotations
            result = result || Arrays.asList(clazz.getDeclaredMethods()).stream()
                .anyMatch(m -> m.getAnnotation(Iri.class) != null || m.getAnnotation(IriNs.class) != null);
        //}

        return result;
    }


    /**
     * Utility method for easing implementations of 'getOrSet' default methods on resource views.
     *
     * Example usage:
     * <pre>
     * {@code
     * class QualifiedDerivation {
     *   @Iri(ProvTerms.hadActivity)
     *   Activity getHadActivity();
     *   QualifiedDerivation setHadActivity(Resource activity);
     *
     *   default Activity getOrSetHadActivity() {
     *     return JenaPluginUtils.getOrSet(this, Activity.class, this::getHadActivity, this::setHadActivity);
     *   }
     * }
     * }
     * </pre>
     *
     * @param <I>
     * @param <O>
     * @param in
     * @param clz
     * @param getter
     * @param setter
     * @return
     */
    public static <I extends RDFNode, O extends RDFNode> O getOrSet(
            I in, Class<O> clz, Supplier<? extends O> getter, Function<? super O, ?> setter) {
        O result = getter.get();
        if (result == null) {
            result = in.getModel().createResource().as(clz);
            setter.apply(result);
        }
        return result;
    }

    public static <I extends RDFNode, O extends RDFNode, C extends Collection<O>>
    O addNew(I in, Class<O> clz, Function<? super I, C> getCollection) {
        return addNewCore(in, clz, () -> getCollection.apply(in), Collection::add);
    }

    public static <I extends RDFNode, X extends RDFNode, C> X addNewCore(
            I in, Class<X> clz, Supplier<C> getter, BiFunction<C, X, ?> adder) {
        C collection = getter.get();
        X item = in.getModel().createResource().as(clz);
        adder.apply(collection, item);

        return item;
    }

}
