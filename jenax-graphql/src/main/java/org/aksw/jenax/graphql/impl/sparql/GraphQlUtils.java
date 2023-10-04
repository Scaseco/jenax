package org.aksw.jenax.graphql.impl.sparql;

import java.util.Collection;
import java.util.Optional;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.jenax.arq.util.expr.NodeValueUtils;
import org.aksw.jenax.arq.util.var.VarUtils;
import org.apache.jena.sparql.expr.NodeValue;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import graphql.language.Argument;
import graphql.language.BooleanValue;
import graphql.language.Field;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.Node;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;

public class GraphQlUtils {
    /** FIXME Update to the graphqls pec*/
    public static boolean isValidCharForFieldName(int ch) {
        return VarUtils.isValidFirstCharForVarName(ch);
    }

    /** Replaces any invalid char with '_' and returns null on empty string */
    public static String safeFieldName(String name) {
        return VarUtils.safeIdentifier(name, '_', GraphQlUtils::isValidCharForFieldName);
    }

    public static Optional<Node<?>> tryGetNode(Node<?> node, String ... path) {
        Node<?> result = node;
        for (String segment : path) {
            if (result == null) {
                break;
            }
            result = result.getNamedChildren().getChildOrNull(segment);
        }
        return Optional.ofNullable(result);
    }

    public static Number getNumber(Node<?> node, String ... path) {
        return tryGetNode(node, path).map(GraphQlUtils::toNumber).orElse(null);
    }

    public static Long getLong(Node<?> node, String ... path) {
        Number number = getNumber(node, path);
        return number == null ? null : number.longValue();
    }

    /** Bridge graphql nodes to jena NodeValues (the latter has a nicer API) */
    public static NodeValue toNodeValue(Node<?> node) {
        NodeValue result = null;
        if (node instanceof IntValue) {
            result = NodeValue.makeInteger(((IntValue)node).getValue());
        } else if (node instanceof FloatValue) {
            result = NodeValue.makeDecimal(((FloatValue)node).getValue());
        } else if (node instanceof BooleanValue) {
            result = NodeValue.makeBoolean(((BooleanValue)node).isValue());
        } else if (node instanceof StringValue) {
            result = NodeValue.makeString(((StringValue)node).getValue());
        }
        return result;
    }

    public static Number toNumber(Node<?> node) {
        NodeValue nv = toNodeValue(node);
        Number result = nv == null ? null : NodeValueUtils.getNumber(nv);
        return result;
    }

    public static Long toLong(Node<?> node) {
        Number number = toNumber(node);
        Long result = number == null ? null : number.longValue();
        return result;
    }

    public static Multimap<String, Value<?>> indexArguments(Field field) {
        Multimap<String, Value<?>> result = Multimaps.transformValues(
                Multimaps.index(field.getArguments(), Argument::getName), Argument::getValue);
        return result;
        // field.getArguments().stream().collect(null)
    }

    public static Multimap<String, Value<?>> indexValues(ObjectValue field) {
        Multimap<String, Value<?>> result = Multimaps.transformValues(
                Multimaps.index(field.getObjectFields(), ObjectField::getName), ObjectField::getValue);
        return result;
    }

    public static Value<?> getArgumentValue(Multimap<String, Value<?>> args, String argName) {
        Collection<Value<?>> a = args.get(argName);
        Value<?> result = IterableUtils.expectZeroOrOneItems(a);
        //Value<?> result = arg == null ? null : arg.getValue();
        return result;
    }

}
