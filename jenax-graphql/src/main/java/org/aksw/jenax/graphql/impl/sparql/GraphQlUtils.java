package org.aksw.jenax.graphql.impl.sparql;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.commons.path.core.Path;
import org.aksw.commons.path.core.PathStr;
import org.aksw.facete.v3.api.TreeDataMap;
import org.aksw.jenax.arq.util.expr.NodeValueUtils;
import org.aksw.jenax.arq.util.var.VarUtils;
import org.apache.jena.sparql.expr.NodeValue;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import graphql.language.Argument;
import graphql.language.BooleanValue;
import graphql.language.Directive;
import graphql.language.EnumValue;
import graphql.language.Field;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.Node;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.SelectionSet;
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

    public static String getString(Node<?> node, String ... path) {
        return tryGetNode(node, path)
                .map(GraphQlUtils::toNodeValue)
                .map(NodeValue::asNode)
                .map(org.apache.jena.graph.Node::getLiteralLexicalForm)
                .orElse(null);
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
        } else if (node instanceof EnumValue) {
            result = NodeValue.makeString(((EnumValue)node).getName());
        }
        return result;
    }

    public static String toString(Node<?> node) {
        NodeValue nv = toNodeValue(node);
        String result = nv == null ? null : nv.getString();
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

    public static Optional<Value<?>> tryGetArgumentValue(Multimap<String, Value<?>> args, String argName) {
        Value<?> value = getArgumentValue(args, argName);
        return Optional.ofNullable(value);
    }

    public static Value<?> getValue(Argument arg) {
        return arg == null ? null : arg.getValue();
    }

    public static TreeDataMap<Path<String>, Field> indexFields(SelectionSet selectionSet) {
        TreeDataMap<Path<String>, Field> result = new TreeDataMap<>();
        Path<String> path = PathStr.newAbsolutePath();
        indexFields(result, path, selectionSet);
        return result;
    }

    public static void indexFields(TreeDataMap<Path<String>, Field> tree, Path<String> path, SelectionSet selection) {
        if (selection != null) {
            List<Field> list = selection.getSelectionsOfType(Field.class);
            if (list != null) {
                for (Field childField : list) {
                    indexField(tree, path, childField);
                }
            }
        }
    }

    public static String getArgValueAsString(Directive directive, String argName) {
        String result = GraphQlUtils.toString(GraphQlUtils.getValue(directive.getArgument(argName)));
        return result;
    }

    public static void indexField(TreeDataMap<Path<String>, Field> tree, Path<String> path, Field field) {
        String fieldName = field.getName();
        Path<String> fieldPath = path.resolve(fieldName);
        tree.putItem(fieldPath, Path::getParent);
        tree.put(fieldPath, field);
        SelectionSet selectionSet = field.getSelectionSet();
        indexFields(tree, fieldPath, selectionSet);
    }
}
