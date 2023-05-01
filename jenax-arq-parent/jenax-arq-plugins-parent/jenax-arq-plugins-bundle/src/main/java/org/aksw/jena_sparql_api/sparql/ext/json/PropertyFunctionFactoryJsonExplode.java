package org.aksw.jena_sparql_api.sparql.ext.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.aksw.jenax.annotation.reprogen.DefaultValue;
import org.aksw.jenax.arq.datatype.RDFDatatypeBinding;
import org.aksw.jenax.arq.functionbinder.FunctionBinders;
import org.aksw.jenax.arq.util.expr.FunctionUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.function.Function;
import org.apache.jena.sparql.pfunction.PFuncSimpleAndList;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Make all keys of a json object available as sparql variables.
 * For json arrays, variables are named ?prefix_{offset + index}
 *
 * {
 *    Bind("['foo', 'bar']"^^xsd:json As ?json)
 *    ?json json:explode ("prefix_", 1).
 * }
 *
 * @author raven
 *
 */
public class PropertyFunctionFactoryJsonExplode
    implements PropertyFunctionFactory {

    // The jena wrapper for the explode function
    public static Function FN_EXPLODE;

    static {
        try {
            FN_EXPLODE = FunctionBinders.getDefaultFunctionBinder().getFunctionGenerator()
                    .wrap(PropertyFunctionFactoryJsonExplode.class.getDeclaredMethod(
                            "explode", JsonElement.class, String.class, Integer.TYPE));
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /** The function being wrapped by FN_EXPLODE */
    public static Binding explode(JsonElement json, @DefaultValue("_") String varPrefix, @DefaultValue("0") int offset) {
        // JsonElement json = RDFDatatypeJson.extractOrNull(node);
        Binding result = explode(json, null, varPrefix, offset);
        return result;
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
                Node node = subject.isVariable()
                        ? binding.get((Var)subject)
                        : subject;

                List<Node> args = new ArrayList<>(1 + objects.getArgListSize());
                args.add(node);
                args.addAll(objects.getArgList());

                Node n = FunctionUtils.invokeWithNodes(FN_EXPLODE, args);
                Binding contrib = RDFDatatypeBinding.extractBinding(n);

                Binding tmp = BindingFactory.builder(binding).addAll(contrib).build();

                QueryIterator result = ofNullableBinding(tmp, execCxt);

                return result;
            }
        };
    }

    public static QueryIterator ofNullableBinding(Binding b, ExecutionContext execCxt) {
        return b == null
            ? QueryIterNullIterator.create(execCxt)
            : QueryIterSingleton.create(b, execCxt);
    }


    public static Binding explode(Node node, Binding parent, String varPrefix, int offset) {
        JsonElement json = JenaJsonUtils.extractOrNull(node);
        Binding result = explode(json, parent, varPrefix, offset);
        return result;
    }


    public static Binding explode(JsonElement json, Binding parent, String varPrefix, int offset) {
        Binding result;

        if (json == null) {
            throw new ExprEvalException("Not a json element");
        } else if (json.isJsonArray()) {
            JsonArray arr = json.getAsJsonArray();
            result = explode(arr, parent, varPrefix, offset);
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            result = explode(obj, parent, varPrefix);
        } else {
            throw new ExprEvalException("Neither a json array nor a json object");
        }

        return result;
    }

    public static Binding explode(JsonArray arr, Binding parent, String varPrefix, int offset) {
        BindingBuilder builder = BindingFactory.builder(parent);
        int size = arr.size();
        for (int i = 0; i < size; ++i) {
            JsonElement item = arr.get(i);

            String str = Integer.toString(offset + i);
            if (varPrefix != null && !varPrefix.isEmpty()) {
                str = varPrefix + str;
            }

            Var v = Var.alloc(str);
            Node n = JenaJsonUtils.createLiteralByValue(item);

            builder.add(v, n);
        }

        Binding result = builder.build();
        return result;
    }

    public static Binding explode(JsonObject obj, Binding parent, String varPrefix) {
        BindingBuilder builder = BindingFactory.builder(parent);
        for (Entry<String, JsonElement> e : obj.entrySet()) {
            String str = e.getKey();
            if (varPrefix != null && !varPrefix.isEmpty()) {
                str = varPrefix + str;
            }

            Var v = Var.alloc(str);
            Node n = JenaJsonUtils.createLiteralByValue(e.getValue());

            builder.add(v, n);
        }

        Binding result = builder.build();
        return result;
    }
}