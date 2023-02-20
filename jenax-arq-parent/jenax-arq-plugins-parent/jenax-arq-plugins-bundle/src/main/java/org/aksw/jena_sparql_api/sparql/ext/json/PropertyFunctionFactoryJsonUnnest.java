package org.aksw.jena_sparql_api.sparql.ext.json;

import org.aksw.jenax.arq.util.binding.BindingUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.pfunction.PFuncSimpleAndList;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;

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

                QueryIterator result = JenaJsonUtils.unnestJsonArray(gson, binding, index, execCxt, node, outputVar);

                return result;
            }
        };
    }
}