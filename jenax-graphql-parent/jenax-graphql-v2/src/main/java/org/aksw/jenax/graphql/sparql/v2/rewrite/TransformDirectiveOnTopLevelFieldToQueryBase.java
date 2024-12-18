package org.aksw.jenax.graphql.sparql.v2.rewrite;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import graphql.language.Directive;
import graphql.language.DirectivesContainer;
import graphql.language.Field;
import graphql.language.Node;
import graphql.language.NodeVisitorStub;
import graphql.language.OperationDefinition;
import graphql.language.OperationDefinition.Operation;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import graphql.util.TreeTransformerUtil;

/** Base class for transforms that move a directive on a top level field to the query level. */
public class TransformDirectiveOnTopLevelFieldToQueryBase
    extends NodeVisitorStub
{
    protected String targetDirectiveName;

    public TransformDirectiveOnTopLevelFieldToQueryBase(String targetDirectiveName) {
        super();
        this.targetDirectiveName = Objects.requireNonNull(targetDirectiveName);
    }

    /** Also assign an ID to the query itself. */
    @Override
    public TraversalControl visitOperationDefinition(OperationDefinition node, TraverserContext<Node> context) {
        if (!context.isVisited() && Operation.QUERY.equals(node.getOperation()) && !node.hasDirective(targetDirectiveName)) {
            boolean hasFieldWithDirective = node.getSelectionSet().getSelectionsOfType(Field.class).stream()
                    .anyMatch(field -> field.hasDirective(targetDirectiveName));

            if (hasFieldWithDirective) {
                transform(targetDirectiveName, node, context, newDirectives -> {
                    OperationDefinition newNode = node.transform(builder -> builder.directives(newDirectives));
                    TreeTransformerUtil.changeNode(context, newNode);
                });
            }
        }

        return super.visitOperationDefinition(node, context);
    }

    public static <T extends DirectivesContainer<?>> TraversalControl transform(String targetDirectiveName, T node, TraverserContext<Node> context, Consumer<List<Directive>> action) {
        if (!context.isVisited()) {
            LinkedList<Directive> remainingDirectives = node.getDirectives().stream()
                    .filter(directive -> !targetDirectiveName.equals(directive.getName()))
                    .collect(Collectors.toCollection(LinkedList::new));

            Directive d = Directive.newDirective()
                .name(targetDirectiveName)
                .build();
            remainingDirectives.addFirst(d);
            action.accept(remainingDirectives);
        }
        return TraversalControl.CONTINUE;
    }
}
