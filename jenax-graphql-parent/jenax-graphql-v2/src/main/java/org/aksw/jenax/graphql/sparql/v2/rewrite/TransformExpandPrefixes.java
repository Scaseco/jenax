package org.aksw.jenax.graphql.sparql.v2.rewrite;

import java.util.LinkedList;
import java.util.stream.Collectors;

import org.aksw.jenax.graphql.sparql.v2.util.GraphQlUtils;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapStd;

import graphql.language.Argument;
import graphql.language.Directive;
import graphql.language.Field;
import graphql.language.Node;
import graphql.language.NodeVisitorStub;
import graphql.language.Value;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import graphql.util.TreeTransformerUtil;

/**
 * Expand prefixes in the following directives:
 * <ul>
 *   <li>{@code @pattern(of: "?s rdfs:label ?o")}</li>
 *   <li>{@code @rdf(iri: "rdf:type")}</li>
 * </ul>
 */
public class TransformExpandPrefixes
    extends NodeVisitorStub
{
    @Override
    public TraversalControl visitField(Field field, TraverserContext<Node> context) {
        PrefixMap prefixes = new PrefixMapStd();

        LinkedList<Directive> remainingDirectives = field.getDirectives().stream()
                .filter(directive -> !process(directive, prefixes))
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

    protected boolean process(Directive node, PrefixMap prefixes) {
        boolean result = false;
        String name = node.getName();
        switch (name) {
        case "prefix": {
            String prefix = GraphQlUtils.getArgAsString(node, "name");
            String iri = GraphQlUtils.getArgAsString(node, "iri");
            if (prefix != null && iri != null) { // XXX Validate
                prefixes.add(prefix, iri);
            }

            Value<?> map = GraphQlUtils.getArgValue(node, "map");
            if (map != null) {
                JenaGraphQlUtils.readPrefixMap(prefixes, map);
            }
            result = true;
            break;
        }
        default:
            break;
        }
        return result;
    }
}
