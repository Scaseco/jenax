package org.aksw.jena_sparql_api.sparql.ext.json;

import java.util.List;

import org.aksw.jena_sparql_api.sparql.ext.util.PropFuncArgUtils;
import org.aksw.jenax.arq.util.binding.BindingUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionBase;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;

import com.google.gson.Gson;

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
        return new PropertyFunctionBase() {
            @Override
            public QueryIterator exec(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {
                Node node = BindingUtils.getValue(binding, argSubject.getArg());

                List<Node> objects = PropFuncArgUtils.getAsList(argObject);
                Node object = objects.get(0);

                Node indexKey = objects.size() > 1 ? objects.get(1) : null;
                Node index = BindingUtils.getValue(binding, indexKey, indexKey);

                if(!object.isVariable()) {
                    throw new RuntimeException("Object of json array unnesting must be a variable");
                }
                Var outputVar = (Var)object;

                QueryIterator result = JenaJsonUtils.unnestJsonArray(gson, binding, index, execCxt, node, outputVar);
                return result;
            }
        };
    }
}
