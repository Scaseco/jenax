package org.aksw.jenax.graphql.sparql.v2.util;

import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.jena.sparql.expr.NodeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.com.google.common.collect.Multimap;
import graphql.com.google.common.collect.Multimaps;
import graphql.language.Argument;
import graphql.language.ArrayValue;
import graphql.language.AstPrinter;
import graphql.language.AstTransformer;
import graphql.language.BooleanValue;
import graphql.language.Directive;
import graphql.language.Directive.Builder;
import graphql.language.DirectivesContainer;
import graphql.language.Document;
import graphql.language.EnumValue;
import graphql.language.Field;
import graphql.language.FieldDefinition;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.Node;
import graphql.language.NodeVisitorStub;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.OperationDefinition;
import graphql.language.OperationDefinition.Operation;
import graphql.language.ScalarValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.language.VariableReference;
import graphql.util.TraverserContext;
import graphql.util.TreeTransformerUtil;

public class GraphQlUtils {

    private static final Logger logger = LoggerFactory.getLogger(GraphQlUtils.class);

    public static String safeName(String name) {
        return replaceIllegalChars(name, "_");
    }

    public static String replaceIllegalChars(String name, String replacement) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            if (isValidNameChar(c, i)) {
                sb.append(c);
            } else {
                sb.append(replacement);
            }
        }
        String result = sb.toString();
        return result;
    }

    /** Whether the char is valid at the given index. */
    public static boolean isValidNameChar(char c, int index) {
        boolean result = index == 0
            ? c == '_' || Character.isLetter(c)
            : c == '_' || Character.isLetterOrDigit(c);
        return result;
    }

    /**
     * Creates a copy of a node with the given list of directives.
     * Returns the new node.
     *
     * replaceDirectives(node, context, (n, newDirectives) -> n.transform(builder -> builder.directives(newDirectives)), remainingDirectives)
     */
    public static <T extends Node<T>> T replaceDirectivesOld(T node, TraverserContext<Node> context,
            BiFunction<T, List<Directive>, T> transform, LinkedList<Directive> remainingDirectives) {
        List<Directive> finalRemainingDirectives = remainingDirectives;
        T replacementNode = transform.apply(node, finalRemainingDirectives);
        TreeTransformerUtil.changeNode(context, replacementNode);
        return replacementNode;
    }

    public static <T extends Node<T>> T replaceDirectives(T node, TraverserContext<Node> context,
            Function<T, Function<List<Directive>, T>> transform, List<Directive> remainingDirectives) {
        List<Directive> finalRemainingDirectives = remainingDirectives;
        Function<List<Directive>, T> factory = transform.apply(node);
        // T replacementNode = transform.apply(node, finalRemainingDirectives);
        T replacementNode = factory.apply(finalRemainingDirectives);
        TreeTransformerUtil.changeNode(context, replacementNode);
        return replacementNode;
    }

    public static Function<List<Directive>, Field> directivesSetterField(Field node) {
        return newDirectives -> node.transform(builder -> builder.directives(newDirectives));
    }

    public static Function<List<Directive>, FieldDefinition> directivesSetterFieldDefinition(FieldDefinition node) {
        return newDirectives -> node.transform(builder -> builder.directives(newDirectives));
    }

    /** FIXME Update to the graphqls pec*/
//    public static boolean isValidCharForFieldName(int ch) {
//        return VarUtils.isValidFirstCharForVarName(ch);
//    }
//
//    /** Replaces any invalid char with '_' and returns null on empty string */
//    public static String safeFieldName(String name) {
//        return VarUtils.safeIdentifier(name, '_', GraphQlUtils::isValidCharForFieldName);
//    }

    public static boolean hasQueryDirective(Document document, String directiveName) {
        List<Directive> matches = getQueryDirectives(document, directiveName);
        boolean result = !matches.isEmpty();
        return result;
    }

    public static List<Directive> getQueryDirectives(Document document, String directiveName) {
        List<Directive> result;
        if (document != null) {
            // Find an OperationDefinition of type Query with the given directive name present
            result = document.getDefinitionsOfType(OperationDefinition.class).stream()
                .filter(od -> Operation.QUERY.equals(od.getOperation()))
                .flatMap(od -> od.getDirectives(directiveName).stream())
                .collect(Collectors.toList());
        } else {
            result = List.of();
        }

        return result;
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

//    public static Value<?> getArgumentValue(Multimap<String, Value<?>> args, String argName) {
//        Collection<Value<?>> a = args.get(argName);
//        Value<?> result = Iterables.getOnlyElement(a, null);
//        //Value<?> result = arg == null ? null : arg.getValue();
//        return result;
//    }

//    public static Value<?> getArgumentValue(Multimap<String, Value<?>> args, String argName, Map<String, Value<?>> assignments) {
//        return resolveValue(getArgumentValue(args, argName), assignments);
//    }
//
//    public static Optional<Value<?>> tryGetArgumentValue(Multimap<String, Value<?>> args, String argName) {
//        Value<?> value = getArgumentValue(args, argName);
//        return Optional.ofNullable(value);
//    }

    public static Value<?> getValue(Argument arg) {
        return arg == null ? null : arg.getValue();
    }

//    public static TreeDataMap<Path<String>, Field> indexFields(SelectionSet selectionSet) {
//        TreeDataMap<Path<String>, Field> result = new TreeDataMap<>();
//        Path<String> path = PathStr.newAbsolutePath();
//        indexFields(result, path, selectionSet);
//        return result;
//    }
//
//    public static void indexFields(TreeDataMap<Path<String>, Field> tree, Path<String> path, SelectionSet selection) {
//        if (selection != null) {
//            List<Field> list = selection.getSelectionsOfType(Field.class);
//            if (list != null) {
//                for (Field childField : list) {
//                    indexField(tree, path, childField);
//                }
//            }
//        }
//    }

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

//    public static void indexField(TreeDataMap<Path<String>, Field> tree, Path<String> path, Field field) {
//        String fieldName = field.getName();
//        Path<String> fieldPath = path.resolve(fieldName);
//        tree.putItem(fieldPath, Path::getParent);
//        tree.put(fieldPath, field);
//        SelectionSet selectionSet = field.getSelectionSet();
//        indexFields(tree, fieldPath, selectionSet);
//    }

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

    public static void println(PrintStream printStream, Document doc) {
        String str = AstPrinter.printAst(doc);
        printStream.println(str);
    }

    public static Directive expectAtMostOneDirective(DirectivesContainer<?> container, String name) {
        List<Directive> directives = container.getDirectives(name);
        if (directives.size() > 1) {
            // TODO log error to graphql processor and return last
            logger.warn("Only one directive expected: " + name);
        }
        return directives.isEmpty() ? null : directives.get(directives.size() - 1);
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

    public static Argument newArgString(String name, String value) {
        return value == null ? null : Argument.newArgument()
                .name(name)
                .value(StringValue.of(value))
                .build();
    }

    public static Argument newArgBoolean(String name, Boolean value) {
        return value == null ? null : Argument.newArgument()
                .name(name)
                .value(BooleanValue.of(value))
                .build();
    }

    public static Argument newArgString(String name, List<String> values) {
        return values == null ? null : Argument.newArgument()
                .name(name)
                .value(toArrayValue(values))
                .build();
    }

    public static Directive newDirective(String name, Argument... arguments) {
        Builder builder = Directive.newDirective()
                .name(name);
        for (Argument arg : arguments) {
            if (arg != null) {
                builder = builder.argument(arg);
            }
        }
        return builder.build();
    }

    public static void removeDirectivesByName(Collection<Directive> directives, String name) {
        directives.removeIf(d -> d.getName().equals(name));
    }
}
