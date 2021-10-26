package org.aksw.jena_sparql_api.mapper.model;

import java.util.function.Function;

import org.aksw.jena_sparql_api.mapper.impl.type.RdfTypeFactoryImpl;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;

/**
 * Convert a Java object to an RDF node.
 * Complex objects are represented as an IRI.
 *
 * @author raven
 *
 */
public class F_ObjectToNode
    implements Function<Object, Node>
{
    protected TypeMapper typeMapper;
    protected RdfTypeFactoryImpl rdfClassFactory;

    public F_ObjectToNode(RdfTypeFactoryImpl rdfClassFactory, TypeMapper typeMapper) {
        this.rdfClassFactory = rdfClassFactory;
        this.typeMapper = typeMapper;
    }

    @Override
    public Node apply(Object o) {
        Node result;
        Class<?> clazz = o.getClass();

        if(String.class.isAssignableFrom(clazz)) {
            // Check whether and which language tags need to be applied
        }

        if(clazz.isPrimitive()) {
            // Use jena's type mapper
            result = NodeUtils.createTypedLiteral(typeMapper, o);
        } else {
            RdfType rdfType = rdfClassFactory.forJavaType(clazz);
            result = rdfType.getRootNode(o);
        }


        return result;
    }
}
