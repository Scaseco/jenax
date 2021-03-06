package org.aksw.jena_sparql_api.sparql.ext.json;

import java.util.Collections;
import java.util.Iterator;
import java.util.stream.IntStream;

import org.aksw.jena_sparql_api.rdf.collections.NodeMapperFromRdfDatatype;
import org.aksw.jenax.arq.util.binding.BindingUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
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
import org.apache.jena.sparql.pfunction.PFuncSimpleAndList;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;
import org.rdfhdt.hdt.iterator.utils.Iter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * {
 *    Bind("['foo', 'bar']"^^xsd:json As ?json)
 *    ?json json:array ?items.
 * }
 *
 * @author raven
 *
 */
public class PropertyFunctionFactoryJsonUnnest
    implements PropertyFunctionFactory {

    protected Gson gson;

    public PropertyFunctionFactoryJsonUnnest() {
        this(new Gson());
    }

    public PropertyFunctionFactoryJsonUnnest(Gson gson) {
        super();
        this.gson = gson;
    }



    @Override
    public PropertyFunction create(final String uri)
    {
        // TODO Allow indexed access if the index variable is bound
        // Consider reusing ListBaseList or listIndex
        return new PFuncSimpleAndList()
        {
            @Override
            public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, PropFuncArg objects,
                    ExecutionContext execCxt) {

                // Get the subject's value
                Node node = BindingUtils.getValue(binding, subject);

                Node object = objects.getArg(0);

                if(!object.isVariable()) {
                    throw new RuntimeException("Object of json array unnesting must be a variable");
                }
                Var outputVar = (Var)object;

                Node indexKey = objects.getArgListSize() > 1 ? objects.getArg(1) : null;
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
	                    	throw new ExprEvalException("Index into json array is a literal but not a number: " + index);
	                    }
	                } else {
                    	throw new ExprEvalException("Index into json array is not a number " + index);
	                }
                }
                Var indexVar = indexVarTmp;


                QueryIterator result = null;

                boolean isJson = node != null && node.isLiteral() && node.getLiteralDatatype() instanceof RDFDatatypeJson;
                if(isJson) {
                    JsonElement data = (JsonElement)node.getLiteralValue();
                    if(data != null && data.isJsonArray()) {
                        JsonArray arr = data.getAsJsonArray();

                        Iterator<Binding> it;
                        if(indexVal != null) {
                        	Binding b = itemToBinding(binding, arr, indexVal, gson, indexVar, outputVar);
                            it = Collections.singleton(b).iterator();
                        } else {
                            it = IntStream.range(0, arr.size()).mapToObj(i -> {
                                Binding r = itemToBinding(binding, arr, i, gson, indexVar, outputVar);
                                return r;
                            }).iterator();
                        }
                        result = QueryIterPlainWrapper.create(it, execCxt);
                    }
                }

                if(result == null) {
                    result = QueryIterNullIterator.create(execCxt);
                }

                return result;
            }
        };
    }

    public static Binding itemToBinding(Binding binding, JsonArray arr, int i, Gson gson, Var indexVar, Var outputVar) {
        JsonElement item;

        try {
            item = arr.get(i);
        } catch(Exception e) {
            throw new ExprEvalException(e);
        }
        RDFDatatype jsonDatatype = TypeMapper.getInstance().getTypeByClass(JsonElement.class);

        Node n = JenaJsonUtils.jsonToNode(item, gson, jsonDatatype);
        // NodeValue nv = n == null ? null : NodeValue.makeNode(n);

        if (n != null) {
            binding = BindingFactory.binding(binding, outputVar, n);
        }

        if(indexVar != null) {
            binding = BindingFactory.binding(binding, indexVar, NodeValue.makeInteger(i).asNode());
        }

        return binding;
    }
}