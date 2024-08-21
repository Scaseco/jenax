package org.aksw.jenax.graphql.sparql.v2.rewrite;

import java.util.LinkedList;
import java.util.stream.Collectors;

import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapStd;

import graphql.language.Argument;
import graphql.language.Directive;
import graphql.language.Field;
import graphql.language.Node;
import graphql.language.NodeVisitorStub;
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
    public TraversalControl visitField(Field field, TraverserContext<Node> context) {
        PrefixMap prefixes = new PrefixMapStd();
        LinkedList<Directive> remainingDirectives = field.getDirectives().stream()
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

            Field newField = field.transform(builder -> builder.directives(remainingDirectives));
            TreeTransformerUtil.changeNode(context, newField);
        }
        return TraversalControl.CONTINUE;
    }
}
