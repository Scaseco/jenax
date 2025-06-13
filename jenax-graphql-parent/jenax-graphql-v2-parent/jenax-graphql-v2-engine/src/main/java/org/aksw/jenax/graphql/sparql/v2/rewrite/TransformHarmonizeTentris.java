package org.aksw.jenax.graphql.sparql.v2.rewrite;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

import org.aksw.jenax.graphql.sparql.v2.model.XGraphQlConstants;
import org.aksw.jenax.graphql.sparql.v2.schema.GraphQlSchemaUtils;
import org.aksw.jenax.graphql.sparql.v2.schema.GraphQlSchemaUtils.TypeInfo;
import org.aksw.jenax.graphql.util.GraphQlUtils;

import graphql.language.Directive;
import graphql.language.DirectivesContainer;
import graphql.language.FieldDefinition;
import graphql.language.Node;
import graphql.language.ObjectTypeDefinition;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;

/** Adapter that converts directives from the Tentris system to those of our system. */
public class TransformHarmonizeTentris
    extends TransformDirectivesBase
{
    @Override
    protected <T extends Node<T>> TraversalControl process(String nodeName, T node, DirectivesContainer<?> directives, TraverserContext<Node> context, BiFunction<T, List<Directive>, T> transform) {
//        // boolean isRootField = isRootField(field, context);
//        PrefixMap pm = getEffectivePrefixMap(context);
//        // PrefixMapping pming = new PrefixMappingAdapter(pm);
//
//        // If we don't have a pattern yet but there is a vocab then create one
//        String vocabIri = processVocab(field, context);
//
//        // Tentative fieldIri
//        String fieldIri = vocabIri == null ? null : vocabIri + nodeName;

        boolean change = false;

        boolean hasInverse = directives.hasDirective("inverse");
        LinkedList<Directive> remainingDirectives = new LinkedList<>(directives.getDirectives());

        if (hasInverse) {
            remainingDirectives.removeIf(x -> "inverse".equals(x.getName()));
            remainingDirectives.add(Directive.newDirective().name("reverse").build());
            change = true;
        }

        boolean hasUri = directives.hasDirective("uri");
        if (hasUri) {
            // Directive uri = ListUtils.last(field.getDirectives("uri"));
            String iriStr = JenaGraphQlUtils.parseRdfIri(nodeName, directives, "uri", "value", null, null);
            GraphQlUtils.removeDirectivesByName(remainingDirectives, "uri");

            boolean isClass = node instanceof ObjectTypeDefinition;
            // boolean isClass = node instanceof FieldDefinition;
            if (isClass) {
                remainingDirectives.addFirst(GraphQlUtils.newDirective(XGraphQlConstants.type, GraphQlUtils.newArgString("iri", iriStr)));
            } else {
                remainingDirectives.addFirst(GraphQlUtils.newDirective("rdf", GraphQlUtils.newArgString("iri", iriStr)));
            }

            change = true;
        }

        if (change) {
            GraphQlUtils.replaceDirectivesOld(node, context, transform, remainingDirectives);
        }

        return TraversalControl.CONTINUE;
    }

    @Override
    public TraversalControl visitFieldDefinitionActual(FieldDefinition node, TraverserContext<Node> context) {
        boolean isIdField = isIdField(node);
        if (isIdField) {
            LinkedList<Directive> remainingDirectives = new LinkedList<>(node.getDirectives());
            remainingDirectives.addFirst(GraphQlUtils.newDirective("to"));
            node = GraphQlUtils.replaceDirectives(node, context, GraphQlUtils::directivesSetterFieldDefinition, remainingDirectives);
        }
        return super.visitFieldDefinitionActual(node, context);
    }

    public static boolean isIdField(FieldDefinition node) {
        TypeInfo typeInfo = GraphQlSchemaUtils.extractTypeInfo(node.getType());
        boolean result = isIdField(typeInfo);
        return result;
    }

    public static boolean isIdField(TypeInfo typeInfo) {
        boolean result = typeInfo.getTypeName().equals("ID");
        return result;
    }

//    @Override
//    public TraversalControl visitFragmentSpreadActual(FragmentSpread node, TraverserContext<Node> context) {
//        return TraversalControl.CONTINUE;
//    }
}
