package org.aksw.jenax.graphql.sparql.v2.rewrite;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.jenax.graphql.sparql.v2.model.ElementNode;

import graphql.language.Argument;
import graphql.language.Directive;
import graphql.language.DirectivesContainer;
import graphql.language.Field;
import graphql.language.InlineFragment;
import graphql.language.Node;
import graphql.language.NodeVisitorStub;
import graphql.language.OperationDefinition;
import graphql.language.OperationDefinition.Operation;
import graphql.language.StringValue;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import graphql.util.TreeTransformerUtil;

/**
 * Transforms a GraphQL document such that all query operations, fields are assigned a {@code @globalId(id: "abc")} directive.
 * This allows for fairly easy matching between the nodes of a GraphQL document and the nodes of an {@link ElementNode} tree.
 */
public class TransformAssignGlobalIds
    extends NodeVisitorStub
{
    protected Supplier<String> idGenerator;

    public TransformAssignGlobalIds(Supplier<String> idGenerator) {
        super();
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    public static TransformAssignGlobalIds of(String baseName, int start) {
        int[] nextId = {start};
        return new TransformAssignGlobalIds(() -> baseName + nextId[0]++);
    }

    /** Also assign an ID to the query itself. */
    @Override
    public TraversalControl visitOperationDefinition(OperationDefinition node, TraverserContext<Node> context) {
        return Operation.QUERY.equals(node.getOperation())
            ?	 transform(node, context, newDirectives -> {
                    OperationDefinition newNode = node.transform(builder -> builder.directives(newDirectives));
                    TreeTransformerUtil.changeNode(context, newNode);
                })
            : TraversalControl.CONTINUE;
    }

    @Override
    public TraversalControl visitField(Field field, TraverserContext<Node> context) {
        return transform(field, context, newDirectives -> {
            Field newNode = field.transform(builder -> builder.directives(newDirectives));
            TreeTransformerUtil.changeNode(context, newNode);
        });
    }

    @Override
    public TraversalControl visitInlineFragment(InlineFragment field, TraverserContext<Node> context) {
        return transform(field, context, newDirectives -> {
            InlineFragment newNode = field.transform(builder -> builder.directives(newDirectives));
            TreeTransformerUtil.changeNode(context, newNode);
        });
    }

    public <T extends DirectivesContainer<?>> TraversalControl transform(T node, TraverserContext<Node> context, Consumer<List<Directive>> action) {
        if (!context.isVisited()) {
            LinkedList<Directive> remainingDirectives = node.getDirectives().stream()
                    .filter(directive -> !process(directive))
                    .collect(Collectors.toCollection(LinkedList::new));

            String id = idGenerator.get();

            Directive d = Directive.newDirective()
                .name("globalId")
                .argument(Argument.newArgument(
                    "id",
                    StringValue.of(id))
                    .build())
                .build();
            remainingDirectives.addFirst(d);

            action.accept(remainingDirectives);

            // Field newField = field.transform(builder -> builder.directives(remainingDirectives));
            // return TreeTransformerUtil.changeNode(context, newField);
        }
        return TraversalControl.CONTINUE;
    }

    protected boolean process(Directive node) {
        return "globalId".equals(node.getName());
    }
}
