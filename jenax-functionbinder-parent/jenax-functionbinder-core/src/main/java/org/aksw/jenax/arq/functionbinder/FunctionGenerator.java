package org.aksw.jenax.arq.functionbinder;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.commons.util.convert.ConvertFunctionRaw;
import org.aksw.commons.util.convert.ConvertFunctionRawImpl;
import org.aksw.commons.util.convert.ConverterRegistry;
import org.aksw.commons.util.convert.ConverterRegistryImpl;
import org.aksw.jenax.annotation.reprogen.DefaultValue;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.Function;

import com.google.common.collect.Iterables;


/**
 * Class for generation of wrappers for Java methods that make them usable as
 * {@link Function}s in Jena's SPARQL engine.
 *
 * @author raven
 *
 */
public class FunctionGenerator {

    protected TypeMapper typeMapper;
    protected ConverterRegistry converterRegistry;

    /** Declarations for mapping jena argument java types to jena
     *  For example, jena.GeometryWrapper may be mapped to jts.Geometry
     *
     *  The cardinality is n:1 - so many jena types may be mapped to the same argument type
     */
    // protected Map<Class<?>, Class<?>> argumentTypeMap;

    /** Declarations for mapping java types to jena internal types
     *  For example, jts.Geometry map by remapped to jena.GeometryWrapper
     *
     *  The cardinality is 1:1 - one input type can only map to one jena type
     */
    protected Map<Class<?>, Class<?>> javaToRdfTypeMap;

    /* WKTDatype (subclass of RDFDatatype) as of Jena 4 lacks the info about the corresponding Java class
     * So we have to add support for working around missing java class declaration... */
    protected Map<Class<?>, String> typeByClassOverrides = new HashMap<>();

    public FunctionGenerator() {
        this(TypeMapper.getInstance(), new ConverterRegistryImpl(), new HashMap<>());
    }

    public FunctionGenerator(
            TypeMapper typeMapper,
            ConverterRegistry converterRegistry,
            Map<Class<?>, Class<?>> returnTypeMap) {
        super();
        this.typeMapper = typeMapper;
        this.converterRegistry = converterRegistry;
        this.javaToRdfTypeMap = returnTypeMap;
    }

    public Map<Class<?>, String> getTypeByClassOverrides() {
        return typeByClassOverrides;
    }

    public TypeMapper getTypeMapper() {
        return typeMapper;
    }

    public ConverterRegistry getConverterRegistry() {
        return converterRegistry;
    }

    /**
     * A map for declaring forced conversions from a given input type to
     * a target type. These conversions are applied before consulting
     * jena's TypeMapper.
     *
     * If the target type is a sub-class of {@link Node} then the type mapper
     * is not consulted because in that case the resulting type is already RDF.
     *
     * @return The map of forced conversions.
     */
    public Map<Class<?>, Class<?>> getJavaToRdfTypeMap() {
        return javaToRdfTypeMap;
    }


    public ConvertFunctionRaw getPreConvert(Class<?> targetJavaType, Class<?> internalJavaType) {
        ConvertFunctionRaw preConvert = null;
        if (internalJavaType != null) {
            preConvert = converterRegistry.getConverter(targetJavaType, internalJavaType);

            if (preConvert == null) {
                throw new RuntimeException(String.format("Conversion from %1$s to %2$s declared but no converter found",
                        targetJavaType, internalJavaType));
            }
        }

        return preConvert;
    }

    public Function wrap(Method method) {
        return wrap(method, null);
    }


    /**
     * Pendant counterpart to Guava's:
     * Iterators.getNext(Iterators.filter(Arrays.asList(arr).iterator(), type), null)
     *
     * @param <T>
     * @param arr
     * @param type
     * @return
     */
//	@SuppressWarnings("unchecked")
//	public static <T> List<T> findItemsByType(Object[] arr, Class<T> type) {
//		ArrayList<T> result = new ArrayList<>();
//		for (int i = 0; i < arr.length; ++i) {
//			Object item = arr[i];
//
//			if (item != null && type.isAssignableFrom(item.getClass())) {
//				result.add((T)item);
//			}
//		}
//
//		return result;
//	}


    public Function wrap(Method method, Object invocationTarget) {
        // Set up conversion of the result value
        java.util.function.Function<Object, NodeValue> returnValueConverter;
        {
            ConvertFunctionRaw resultConverter;
            Class<?> targetJavaType = method.getReturnType();
            // AnnotatedType art = method.getAnnotatedReturnType();

            // TODO Check for an @IriType annotation that would turn Strings into IRIs

            Class<?> internalJavaType = javaToRdfTypeMap.get(targetJavaType);
            Class<?> workingType = internalJavaType != null ? internalJavaType : targetJavaType;

            ConvertFunctionRaw preConvert = internalJavaType == null ? null : getPreConvert(targetJavaType, internalJavaType);

            ConvertFunctionRaw internalTypeToNodeValue = createNodeValueMapper(
                    workingType,
                    converterRegistry,
                    typeMapper,
                    typeByClassOverrides);


            resultConverter = preConvert == null
                ? internalTypeToNodeValue
                : preConvert.andThen(internalTypeToNodeValue);

            returnValueConverter = in -> in == null ? null : (NodeValue)resultConverter.convertRaw(in);
        }

        // Set up parameter conversions and default values
        int n = method.getParameterCount();
        Class<?>[] pts = method.getParameterTypes();
        // AnnotatedType[] apts = method.getAnnotatedParameterTypes();
        Annotation[][] pas = method.getParameterAnnotations();

        // Once a default value is seen all further parameters must also have a
        // specified default value
        int firstDefaultValueIdx = -1;

        Param[] params = new Param[n];
        for (int i = 0; i < n; ++i) {
            Annotation[] as = pas[i];


            DefaultValue defaultValueAnnotation = IterableUtils.expectZeroOrOneItems(
                    Iterables.filter(Arrays.asList(as), DefaultValue.class));
            Class<?> paramClass = pts[i];

            Class<?> inputClass = javaToRdfTypeMap.get(paramClass);
            Class<?> rdfClass = inputClass != null ? inputClass : paramClass;
            ConvertFunctionRaw inputConverter = inputClass == null ? null : getPreConvert(inputClass, paramClass);

            // Consult override map first because some datatypes may lack appropriate metadata
            String datatypeIri = typeByClassOverrides.get(rdfClass);

            RDFDatatype dtype = datatypeIri != null
                    ? typeMapper.getTypeByName(datatypeIri)
                    : typeMapper.getTypeByClass(rdfClass);

            // RDFDatatype dtype = typeMapper.getTypeByClass(rdfClass);

            // If the pre-conversion already yields Node then we don't need an rdf datatype
            boolean isNodeType = inputClass != null && Node.class.isAssignableFrom(inputClass);

            if (dtype == null) {
                if (!isNodeType) {
                    throw new RuntimeException(String.format("TypeMapper does not contain an entry for the java class %1$s derived from %2$s", inputClass, paramClass));
                }
            }

            Object defaultValue = null;
            if (defaultValueAnnotation != null) {
                if (firstDefaultValueIdx < 0) {
                    firstDefaultValueIdx = i;
                }
                String str = defaultValueAnnotation.value();

                if (str != null) {
                    Object internalObj = dtype.parse(str);
                    defaultValue = FunctionAdapter.convert(internalObj, paramClass, converterRegistry);
                } else {
                    defaultValue = null;
                }
            } else {
                if (firstDefaultValueIdx >= 0) {
                    throw new RuntimeException(String.format(
                            "Parameter at index %d does not declare a default value although a prior parameter at index %d declared one",
                            i, firstDefaultValueIdx));
                }
            }

            Param param = new Param(paramClass, inputClass, inputConverter, defaultValue);
            params[i] = param;
        }

        FunctionAdapter result = new FunctionAdapter(
                method, invocationTarget,
                returnValueConverter, params,
                typeMapper, converterRegistry);

        return result;
    }

    public static ConvertFunctionRaw createNodeValueMapper(
            Class<?> clz,
            ConverterRegistry converterRegistry,
            TypeMapper typeMapper,
            Map<Class<?>, String> typeByClassOverrides) {
        // Check the converterRegistry for a direct conversion
        ConvertFunctionRaw result = converterRegistry.getConverter(clz, NodeValue.class);

        if (result == null) {
            String datatypeIri = typeByClassOverrides.get(clz);

            RDFDatatype dtype = datatypeIri != null
                    ? typeMapper.getTypeByName(datatypeIri)
                    : typeMapper.getTypeByClass(clz);

            // RDFDatatype dtype = typeMapper.getTypeByClass(clz);

            if (dtype == null) {
                throw new RuntimeException(String.format("No RDF datatype registered for %1$s", clz));
            }

            result = new ConvertFunctionRawImpl(clz, NodeValue.class, (Object obj) -> {
                NodeValue r = null;
                if (obj != null) {
                    Node node = NodeFactory.createLiteralByValue(obj, dtype);
                    r = NodeValue.makeNode(node);
                }
                return r;
            });
        }

        return result;
    }
}
