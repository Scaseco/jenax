package org.aksw.jenax.graphql.sparql;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.path.core.PathStr;
import org.aksw.facete.v3.api.TreeDataMap;
import org.aksw.jenax.arq.util.expr.NodeValueUtils;
import org.aksw.jenax.arq.util.var.VarUtils;
import org.apache.jena.sparql.expr.NodeValue;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import graphql.language.Argument;
import graphql.language.ArrayValue;
import graphql.language.AstTransformer;
import graphql.language.BooleanValue;
import graphql.language.Directive;
import graphql.language.DirectivesContainer;
import graphql.language.Document;
import graphql.language.EnumValue;
import graphql.language.Field;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.Node;
import graphql.language.NodeVisitorStub;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.ScalarValue;
import graphql.language.SelectionSet;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.language.VariableReference;

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

    public static ScalarValue<?> toScalarValue(NodeValue nv) {
        ScalarValue<?> result;
        if (nv.isString()) {
            result = StringValue.newStringValue(nv.getString()).build();
        } else  if (nv.isInteger()) {
            result = IntValue.newIntValue(nv.getInteger()).build();
        } else if (nv.isBoolean()) {
            result = BooleanValue.newBooleanValue(nv.getBoolean()).build();
        } else if (nv.isDecimal()) {
            result = FloatValue.newFloatValue(nv.getDecimal()).build();
        } else {
            throw new UnsupportedOperationException("Cannot convert: " + nv);
        }
        return result;
    }

    public static Boolean getArgAsBoolean(Directive directive, String argName) {
        Boolean result = getArgAsBoolean(directive, argName, null);
        return result;
    }

    public static String getArgAsString(Directive directive, String argName) {
        String result = GraphQlUtils.toString(GraphQlUtils.getValue(directive.getArgument(argName)));
        return result;
    }

    /** Expand strings to lists */
    public static List<String> getArgAsStrings(Directive directive, String argName) {
        Value<?> raw = GraphQlUtils.getValue(directive.getArgument(argName));
        List<String> result = raw == null
            ? null
            : raw instanceof ArrayValue arr
                ? arr.getValues().stream().map(v -> toString(v)).toList()
                : List.of(toString(raw));
        return result;
    }

    public static String toString(Node<?> node) {
        NodeValue nv = toNodeValue(node);
        String result = nv == null ? null : nv.getString();
        return result;
    }

    public static Boolean toBoolean(Node<?> node) {
        NodeValue nv = toNodeValue(node);
        Boolean result = nv == null ? null : nv.getBoolean();
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
        Value<?> result = Iterables.getOnlyElement(a, null);
        //Value<?> result = arg == null ? null : arg.getValue();
        return result;
    }

    public static Value<?> getArgumentValue(Multimap<String, Value<?>> args, String argName, Map<String, Value<?>> assignments) {
        return resolveValue(getArgumentValue(args, argName), assignments);
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

    public static Long getArgAsLong(Field field, String name, Map<String, Value<?>> assignments) {
        return getArgAsLong(field.getArguments(),  name, assignments);
    }

    public static Long getArgAsLong(List<Argument> arguments, String name, Map<String, Value<?>> assignments) {
        return toLong(resolveValue(getValue(findArgument(arguments, name)), assignments));
    }

    public static Argument findArgument(List<Argument> arguments, String name) {
        return arguments.stream()
                .filter(arg -> name.equals(arg.getName()))
                .findFirst()
                .orElse(null);
    }

    public static Value<?> getArgValue(Directive directive, String argName) {
        Argument arg = directive.getArgument(argName);
        Value<?> result = arg == null ? null : arg.getValue();
        return result;
    }

    public static String getArgValueAsString(Directive directive, String argName, Map<String, Value<?>> assignments) {
        String result = toString(resolveValue(getValue(directive.getArgument(argName)), assignments));
        return result;
    }

    public static Boolean getArgAsBoolean(Directive directive, String argName, Map<String, Value<?>> assignments) {
        Boolean result = toBoolean(resolveValue(getValue(directive.getArgument(argName)), assignments));
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

    /** Resolves variable references once against the given map of assignments. Null if there is no assignment. */
    public static Value<?> resolveValue(Value<?> value, Map<String, Value<?>> assignments) {
        Value<?> result;
        if (value instanceof VariableReference ref) {
            String varName = ref.getName();
            result = assignments.get(varName);
        } else {
            result = value;
        }
        return result;
    }

    public static Map<String, Value<?>> mapToGraphQl(Map<String, org.apache.jena.graph.Node> assignments) {
        Map<String, Value<?>> result = assignments == null
                ? null
                : assignments.entrySet().stream().collect(Collectors.toMap(
                        Entry::getKey, v -> (Value<?>)GraphQlUtils.toScalarValue(NodeValue.makeNode(v.getValue()))));
        return result;
    }

    public static Map<String, org.apache.jena.graph.Node> mapToJena(Map<String, Value<?>> assignments) {
        Map<String, org.apache.jena.graph.Node> result = assignments == null
                ? null
                : assignments.entrySet().stream().collect(Collectors.toMap(
                        Entry::getKey, v -> GraphQlUtils.toNodeValue(v.getValue()).asNode()));
        return result;
    }


    public static Document applyTransform(Document doc, NodeVisitorStub visitor) {
        AstTransformer transformer = new AstTransformer();
        Node<?> node = transformer.transform(doc, visitor);
        Document result = (Document)node;
        return result;
    }

    public static Directive expectAtMostOneDirective(DirectivesContainer<?> container, String name) {
        List<Directive> directives = container.getDirectives(name);
        if (directives.size() > 1) {
            // TODO log error and return last
            System.err.println("Only one directive expected: " + name);
        }
        return directives.isEmpty() ? null : directives.get(0);
    }

//
//    public static Object transform(Node root, NodeVisitor enterVisitor, NodeVisitor leaveVisitor) {
//        assertNotNull(root);
//        assertNotNull(enterVisitor);
//        assertNotNull(leaveVisitor);
//
//        TraverserVisitor<Node> traverserVisitor = new TraverserVisitor<Node>() {
//            @Override
//            public TraversalControl enter(TraverserContext<Node> context) {
//                return context.thisNode().accept(context, enterVisitor);
//            }
//
//            @Override
//            public TraversalControl leave(TraverserContext<Node> context) {
//                return context.thisNode().accept(context, leaveVisitor);
//            }
//        };
//
//
//        // TraverserVisitor<Node> traverserVisitor = AstTransformer.getNodeTraverserVisitor(nodeVisitor);
//        TreeTransformer<Node> treeTransformer = new TreeTransformer<>(AST_NODE_ADAPTER);
//        return treeTransformer.transform(root, traverserVisitor);
//    }


//    public static Directive expectOneDirective(DirectivesContainer<?> container, String name) {
//    	Directive result = expectAtMostOneDirective(container, name);
//    	if (result == null) {
//    		thro
//    	}
//    }

    public static ArrayValue toArrayValue(List<String> strs) {
        return ArrayValue.newArrayValue().values(strs.stream().map(x -> (Value)StringValue.of(x)).toList()).build();
    }
}
