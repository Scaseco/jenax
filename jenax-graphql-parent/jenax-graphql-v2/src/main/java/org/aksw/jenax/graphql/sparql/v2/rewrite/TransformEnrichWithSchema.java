package org.aksw.jenax.graphql.sparql.v2.rewrite;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.aksw.jenax.graphql.sparql.v2.api2.Connective;
import org.aksw.jenax.graphql.sparql.v2.schema.Fragment;
import org.aksw.jenax.graphql.sparql.v2.schema.SchemaEdge;
import org.aksw.jenax.graphql.sparql.v2.schema.SchemaNavigator;
import org.aksw.jenax.graphql.sparql.v2.schema.SchemaNode;
import org.aksw.jenax.graphql.sparql.v2.schema.SchemaNodeOverObjectTypeDefinition;
import org.aksw.jenax.graphql.sparql.v2.util.GraphQlUtils;

import graphql.language.Argument;
import graphql.language.Directive;
import graphql.language.DirectivesContainer;
import graphql.language.Field;
import graphql.language.Node;
import graphql.language.NodeVisitorStub;
import graphql.language.ObjectTypeDefinition;
import graphql.language.OperationDefinition;
import graphql.language.SelectionSet;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;

public class TransformEnrichWithSchema
    extends NodeVisitorStub
{
    protected SchemaNavigator schemaNavigator;

    public TransformEnrichWithSchema(SchemaNavigator schemaNavigator) {
        super();
        this.schemaNavigator = Objects.requireNonNull(schemaNavigator);
    }

    public TraversalControl enterQuery(OperationDefinition node, TraverserContext<Node> context) {
        if (!context.isVisited()) {
            SchemaNode schemaNode = schemaNavigator.getOrCreateSchemaNode("Query");
            context.setVar(SchemaNode.class, schemaNode);

//            RewriteResult<K> rr = context.getVarFromParents(RewriteResult.class);
//            // Directive stateIdDirective = GraphQlUtils.expectAtMostOneDirective(node, "globalId");
//            String stateId = readStateId(node);
//            // org.apache.jena.graph.Node globalIdNode = globalIdToSparql(stateId);
//
//
//            // An element node with an empty group graph pattern (single binding that does not bind any variables)
//            ElementNode rootElementNode = ElementNode.of("root", Connective.newBuilder().element(new ElementGroup()).connectVars().targetVars().build());
//            rootElementNode.setIdentifier(stateId);
//
//            AggStateBuilderObject<Binding, FunctionEnv, K, org.apache.jena.graph.Node> aggStateBuilderRoot = new AggStateBuilderObject<>();
//            rr.root = new GraphQlFieldRewrite<>(rootElementNode, aggStateBuilderRoot, true, node);
        }

        return TraversalControl.CONTINUE;
    }

    public TraversalControl leaveQuery(OperationDefinition node, TraverserContext<Node> context) {
        return TraversalControl.CONTINUE;
    }

    @Override
    public TraversalControl visitOperationDefinition(OperationDefinition node, TraverserContext<Node> context) {
        TraversalControl result = "QUERY".equals(node.getOperation().name())
            ? !context.isVisited()
                ? enterQuery(node, context)
                : leaveQuery(node, context)
            : super.visitOperationDefinition(node, context);
        return result;
    }

//    @Override
//    public TraversalControl visitDocument(Document node, TraverserContext<Node> context) {
//        return TraversalControl.CONTINUE;
//    }

    @Override
    public TraversalControl visitField(Field field, TraverserContext<Node> context) {
        TraversalControl result = !context.isVisited()
            ? enterField(field, context)
            : leaveField(field, context);
        return result;
    }

    public TraversalControl enterField(Field field, TraverserContext<Node> context) {
        processFieldOnEnter(field, field.getName(), field.getArguments(), field, field.getSelectionSet(), context);
        return TraversalControl.CONTINUE;
    }

    public void processFieldOnEnter(Field node, String nodeName, List<Argument> arguments, DirectivesContainer<?> directives, SelectionSet selectionSet, TraverserContext<Node> context) {
        SchemaNode thisSchemaNode = context.getVarFromParents(SchemaNode.class);
        if (thisSchemaNode != null) {

            SchemaEdge schemaEdge = thisSchemaNode.getEdge(nodeName).orElse(null);
            // SchemaNode nextSchemaNode = thisSchemaNode.getEdge(nodeName).orElse(null);
            SchemaNode nextSchemaNode = schemaEdge != null ? schemaEdge.getTargetSchemaNode() : null;
            context.setVar(SchemaNode.class, nextSchemaNode);

            Fragment fragment = nextSchemaNode.getFragment();
            // System.out.println(fragment);

            boolean hasPattern = hasPattern(node);
            if (!hasPattern) {
                List<Directive> newDirectives = new ArrayList<>(node.getDirectives().size());

                if (schemaEdge.isCardinalityOne()) {
                    newDirectives.add(GraphQlUtils.newDirective("one"));
                } else {
                    newDirectives.add(GraphQlUtils.newDirective("many"));
                }

                // Transfer directives from the field (the 'edge')
                newDirectives.addAll(schemaEdge.getFieldDefinition().getDirectives());

                if (nextSchemaNode instanceof SchemaNodeOverObjectTypeDefinition snotd) {
                    ObjectTypeDefinition otd = snotd.getObjectTypeDefinition();
                    newDirectives.addAll(otd.getDirectives());
                }

                newDirectives.addAll(node.getDirectives());

                // Inherit the pattern from the schema edge
                Connective connective = schemaEdge.getConnective();

                // If we are at the root schema node (usually the one named "Query") then
                // only infer the connective from the target type

//                if (connective != null) {
//                    newDirectives.addFirst(XGraphQlUtils.newDirectivePattern(connective));
//                    GraphQlUtils.replaceDirectives(node, context, GraphQlUtils::directivesSetterField, newDirectives);
//                }
                GraphQlUtils.replaceDirectives(node, context, GraphQlUtils::directivesSetterField, newDirectives);
            }
//            if (nextSchemaNode != null) {
//                System.out.println("here" + nextSchemaNode);
//            }
        }
    }

    public TraversalControl leaveField(Field field, TraverserContext<Node> context) {
        return TraversalControl.CONTINUE;
    }

    public static boolean hasPattern(DirectivesContainer<?> dirs) {
        boolean result = dirs.hasDirective("rdf") || dirs.hasDirective("pattern") || dirs.hasDirective("iri") || dirs.hasDirective("bind");
        return result;
    }

//    @Override
//    protected <T extends Node<T>> TraversalControl process(String nodeName, T node, DirectivesContainer<?> field,
//            TraverserContext<Node> context, BiFunction<T, List<Directive>, T> transform) {
//        return TraversalControl.CONTINUE;
//    }
}
