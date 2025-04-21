package org.aksw.jenax.graphql.sparql.v2.rewrite;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapStd;

import graphql.language.Argument;
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
 * Collects all prefix directives and merges them into a single one. The directive is added to the beginning of the directives list.
 * Order of prefixes on a node does not matter.
 */
public class TransformHarmonizePrefixes
    extends NodeVisitorStub
{
    @Override
    public TraversalControl visitOperationDefinition(OperationDefinition node, TraverserContext<Node> context) {
        return Operation.QUERY.equals(node.getOperation())
            ? TransformHarmonizePrefixes.process(node, node, context,
                  (n, newDirectives) -> n.transform(builder -> builder.directives(newDirectives)))
            : TraversalControl.CONTINUE;
    }

    @Override
    public TraversalControl visitField(Field field, TraverserContext<Node> context) {
        return TransformHarmonizePrefixes.process(field, field, context,
            (node, newDirectives) -> node.transform(builder -> builder.directives(newDirectives)));
    }

    public static <T extends Node<T>> TraversalControl process(T node, DirectivesContainer<?> directives, TraverserContext<Node> context, BiFunction<T, List<Directive>, T> transform) {
        if (!context.isVisited()) {
            PrefixMap prefixes = new PrefixMapStd();
            LinkedList<Directive> remainingDirectives = directives.getDirectives().stream()
                    .filter(directive -> !JenaGraphQlUtils.readPrefixDirective(directive, prefixes))
                    .collect(Collectors.toCollection(LinkedList::new));

            if (!prefixes.isEmpty()) {
                Directive p = Directive.newDirective()
                    .name("prefix")
                    .argument(Argument.newArgument(
                        "map",
                        JenaGraphQlUtils.toObjectValue(prefixes))
                        .build())
                    .build();
                remainingDirectives.addFirst(p);

                T newNode = transform.apply(node, remainingDirectives);
                TreeTransformerUtil.changeNode(context, newNode);
            }
        }
        return TraversalControl.CONTINUE;
    }
}
