package org.aksw.jena_sparql_api.sparql.ext.array;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import org.aksw.jena_sparql_api.rdf.collections.NodeMapperFromRdfDatatype;
import org.aksw.jena_sparql_api.sparql.ext.geosparql.PropFuncArgUtils;
import org.aksw.jenax.arq.datatype.RDFDatatypeNodeList;
import org.aksw.jenax.arq.util.binding.BindingUtils;
import org.aksw.jenax.arq.util.node.NodeList;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionBase;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;

public class PFF_ArrayUnnest
    implements PropertyFunctionFactory {

    @Override
    public PropertyFunction create(String uri) {
        return new PF_ArrayUnnest();
    }

    public static class PF_ArrayUnnest
        extends PropertyFunctionBase {
        @Override
        public QueryIterator exec(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject,
                ExecutionContext execCxt) {

            // Get the subject's value
            Node node = BindingUtils.getValue(binding, argSubject.getArg());

            List<Node> objects = PropFuncArgUtils.getAsList(argObject);
            Node object = objects.get(0);

    //                if(!object.isVariable()) {
    //                    throw new RuntimeException("Object position of array unnesting must be a variable");
    //                }
            // Var outputVar = (Var)object;

            Node indexKey = objects.size() > 1 ? objects.get(1) : null;
            Node index = BindingUtils.getValue(binding, indexKey, indexKey);


            Var indexVarTmp = null;
            Integer indexVal = null;

            if (index != null) {
                if(index.isVariable()) {
                    indexVarTmp = (Var)index;
    //                    throw new RuntimeException("Index of json array unnesting must be a variable");
                } else if(index.isLiteral()) {
                    Object obj = NodeMapperFromRdfDatatype.toJavaCore(index, index.getLiteralDatatype());
                    if(obj instanceof Number) {
                        indexVal = ((Number)obj).intValue();
                    } else {
                        throw new ExprEvalException("Index into node array is a literal but not a number: " + index);
                    }
                } else {
                    throw new ExprEvalException("Index into node array is not a number " + index);
                }
            }
            Var indexVar = indexVarTmp;


            QueryIterator result = null;

            boolean isArray = node != null && node.isLiteral() && node.getLiteralDatatype() instanceof RDFDatatypeNodeList;
            if(isArray) {
                NodeList arr = (NodeList)node.getLiteralValue();

                Iterator<Binding> it;
                if(indexVal != null) {
                    Binding b = itemToBinding(binding, arr, indexVal, indexVar, object);
                    it = b == null ? Collections.emptyIterator() : Collections.singleton(b).iterator();
                } else {
                    it = IntStream.range(0, arr.size()).mapToObj(i -> {
                        Binding r = itemToBinding(binding, arr, i, indexVar, object);
                        return r;
                    })
                    .filter(Objects::nonNull)
                    .iterator();
                }
                result = QueryIterPlainWrapper.create(it, execCxt);
            }

            if(result == null) {
                result = QueryIterNullIterator.create(execCxt);
            }

            return result;
        }

        /**
         * Returns a binding for the item at the given index.
         * Returns null if 'output' is a concrete node that does not match the item at the given index
         * indexVar may be null
         */
        public static Binding itemToBinding(
                Binding binding,
                NodeList arr,
                int i,
                Var indexVar,
                Node output) {
            Node item;

            try {
                item = arr.get(i);
            } catch(Exception e) {
                throw new ExprEvalException(e);
            }

            if (item != null) {
                if (output != null) {
                    if (output.isVariable()) {
                        Var v = (Var)output;
                        binding = BindingFactory.binding(binding, v, item);
                    } else if (!Objects.equals(item, output)) {
                        binding = null;
                    }
                }
            }

            if(binding != null && indexVar != null) {
                binding = BindingFactory.binding(binding, indexVar, NodeValue.makeInteger(i).asNode());
            }

            return binding;
        }
    }
}