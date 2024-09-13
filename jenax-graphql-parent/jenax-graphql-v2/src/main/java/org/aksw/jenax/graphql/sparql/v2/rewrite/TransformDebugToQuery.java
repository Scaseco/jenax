package org.aksw.jenax.graphql.sparql.v2.rewrite;

import java.util.LinkedList;
import java.util.List;
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

/**
 * Adds <pre>@debug</pre> to a query operation if any of its direct children has that directive.
 */
public class TransformDebugToQuery
    extends NodeVisitorStub
{
    /** Also assign an ID to the query itself. */
    @Override
    public TraversalControl visitOperationDefinition(OperationDefinition node, TraverserContext<Node> context) {
        if (!context.isVisited() && Operation.QUERY.equals(node.getOperation()) && !node.hasDirective("debug")) {
            boolean hasFieldWithDebug = node.getSelectionSet().getSelectionsOfType(Field.class).stream()
                    .anyMatch(field -> field.hasDirective("debug"));

            if (hasFieldWithDebug) {
                transform(node, context, newDirectives -> {
                    OperationDefinition newNode = node.transform(builder -> builder.directives(newDirectives));
                    TreeTransformerUtil.changeNode(context, newNode);
                });
            }
        }

        return super.visitOperationDefinition(node, context);
    }

    public <T extends DirectivesContainer<?>> TraversalControl transform(T node, TraverserContext<Node> context, Consumer<List<Directive>> action) {
        if (!context.isVisited()) {
            LinkedList<Directive> remainingDirectives = node.getDirectives().stream()
                    .filter(directive -> !"debug".equals(directive.getName()))
                    .collect(Collectors.toCollection(LinkedList::new));

            Directive d = Directive.newDirective()
                .name("debug")
                .build();
            remainingDirectives.addFirst(d);
            action.accept(remainingDirectives);
        }
        return TraversalControl.CONTINUE;
    }
}
