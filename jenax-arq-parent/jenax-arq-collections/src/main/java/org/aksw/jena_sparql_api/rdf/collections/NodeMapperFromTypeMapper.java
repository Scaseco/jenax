package org.aksw.jena_sparql_api.rdf.collections;

import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.aksw.commons.util.obj.ObjectUtils;
import org.aksw.commons.util.reflect.ClassUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public class NodeMapperFromTypeMapper<T>
    implements NodeMapper<T>
{
    protected TypeMapper typeMapper;
    protected Class<?> viewClass;


    // Base class for acceptable literal types - this only acts as a filter
    public NodeMapperFromTypeMapper(Class<?> viewClass, TypeMapper typeMapper) {
        this.viewClass = viewClass;
        this.typeMapper = typeMapper;
    }

    @Override
    public Class<?> getJavaClass() {
        return viewClass;
    }

    @Override
    public boolean canMap(Node node) {
        boolean result = NodeMapperFromRdfDatatype.canMapCore(node, viewClass);
//
//		boolean result = node.isLiteral();
//		if(node.isLiteral()) {
//			Class<?> literalClass = node.getLiteral().getDatatype().getJavaClass();
//			result = literalClass != null && viewClass.isAssignableFrom(literalClass);
//		} else {
//			result = false;
//		}

        return result;
    }

    @Override
    public T toJava(Node node) {
        Object result = NodeMapperFromRdfDatatype.toJavaCore(node, viewClass);
        return (T)result;
        //return (T)node.getLiteralValue();
    }

    public static RDFDatatype findTypeMapping(TypeMapper typeMapper, Object obj) {
        RDFDatatype result = typeMapper.getTypeByValue(obj);

        if (result == null) {
            Class<?> cls = obj.getClass();
            result = findInHierarchy(typeMapper, cls);
        }
        return result;
    }

    public static RDFDatatype findInHierarchy(TypeMapper typeMapper, Class<?> cls) {
        RDFDatatype result = null;

        Iterator<List<Class<?>>> it = ClassUtils.bfsStream(cls).iterator();
        while (it.hasNext()) {
            List<Class<?>> breadth = it.next();

            Map<Class<?>, RDFDatatype> map = breadth.stream()
                .map(x -> new SimpleEntry<>(x, typeMapper.getTypeByClass(x)))
                .filter(x -> x.getValue() != null)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

            if (!map.isEmpty()) {
                if (map.size() == 1) {
                    result = map.values().iterator().next();
                } else {
                    throw new RuntimeException("Multiple candidate type mappings: " + map);
                }
            }

        }
        return result;
    }

    @Override
    public Node toNode(T obj) {


        // RDFDatatype dtype = typeMapper.getTypeByValue(obj);
        RDFDatatype dtype = findTypeMapping(typeMapper, obj);
        Objects.requireNonNull(dtype, "No datatype found for object of type " + ObjectUtils.getClass(obj) + " with value " + obj);

//		String lex = dtype.unparse(obj);
        //Node result = NodeFactory.createLiteral(lex, dtype);
        Node result = NodeFactory.createLiteralByValue(obj, dtype);

        return result;

//		Node result;
//
//		RDFDatatype dtype = typeMapper.getTypeByValue(obj);
//		if(dtype != null) {
//			result = NodeFactory.createLiteralByValue(obj, dtype);
//		} else {
//			result = null;
//		}
//
//		// TODO Auto-generated method stub
//		return result;
    }

}
