package org.aksw.jenax.graphql.sparql.v2.rewrite;

import java.util.List;
import java.util.function.BiFunction;

import graphql.language.Directive;
import graphql.language.DirectivesContainer;
import graphql.language.Field;
import graphql.language.FieldDefinition;
import graphql.language.InlineFragment;
import graphql.language.InterfaceTypeDefinition;
import graphql.language.Node;
import graphql.language.NodeVisitorStub;
import graphql.language.ObjectTypeDefinition;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;

public abstract class TransformDirectivesBase
    extends NodeVisitorStub
{
//    @Override
//    public TraversalControl visitOperationDefinition(OperationDefinition node, TraverserContext<Node> context) {
//        return super.visitOperationDefinition(node, context);
//    }

    @Override
    public final TraversalControl visitField(Field field, TraverserContext<Node> context) {
        return visitFieldActual(field, context);
    }

    @Override
    public final TraversalControl visitFieldDefinition(FieldDefinition fieldDefinition, TraverserContext<Node> context) {
        return visitFieldDefinitionActual(fieldDefinition, context);
    }


    @Override
    public TraversalControl visitInterfaceTypeDefinition(InterfaceTypeDefinition node, TraverserContext<Node> context) {
        return visitInterfaceTypeDefinitionActual(node, context);
    }

    @Override
    public TraversalControl visitObjectTypeDefinition(ObjectTypeDefinition node, TraverserContext<Node> context) {
        return visitObjectTypeDefinitionActual(node, context);
    }

    @Override
    public final TraversalControl visitInlineFragment(InlineFragment node, TraverserContext<Node> context) {
        return visitInlineFragmentActual(node, context);
    }

    // @Override
//    public TraversalControl visitFragmentSpread(FragmentSpread node, TraverserContext<Node> context) {
//        return visitFragmentSpreadActual(node, context);
//    }


    // @Override
    public TraversalControl visitInlineFragmentActual(InlineFragment node, TraverserContext<Node> context) {
        return process("", node, node, context, (n, newDirectives) -> n.transform(builder -> builder.directives(newDirectives)));
    }

    // @Override
    public TraversalControl visitFieldActual(Field field, TraverserContext<Node> context) {
        return process(field.getName(), field, field, context, (node, newDirectives) -> node.transform(builder -> builder.directives(newDirectives)));
    }

    // @Override
    public TraversalControl visitFieldDefinitionActual(FieldDefinition node, TraverserContext<Node> context) {
        return process(node.getName(), node, node, context, (n, newDirectives) -> n.transform(builder -> builder.directives(newDirectives)));
    }

    // @Override
    public TraversalControl visitInterfaceTypeDefinitionActual(InterfaceTypeDefinition node, TraverserContext<Node> context) {
        return process(node.getName(), node, node, context, (n, newDirectives) -> n.transform(builder -> builder.directives(newDirectives)));
    }

    // @Override
    public TraversalControl visitObjectTypeDefinitionActual(ObjectTypeDefinition node, TraverserContext<Node> context) {
        return process(node.getName(), node, node, context, (n, newDirectives) -> n.transform(builder -> builder.directives(newDirectives)));
    }

    protected abstract <T extends Node<T>> TraversalControl process(String nodeName, T node, DirectivesContainer<?> field, TraverserContext<Node> context, BiFunction<T, List<Directive>, T> transform);
}
