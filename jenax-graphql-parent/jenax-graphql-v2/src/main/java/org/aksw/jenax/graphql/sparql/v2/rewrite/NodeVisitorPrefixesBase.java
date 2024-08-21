package org.aksw.jenax.graphql.sparql.v2.rewrite;

import java.util.List;

import org.aksw.jenax.graphql.sparql.v2.util.PrefixMap2;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;

import graphql.language.Directive;
import graphql.language.DirectivesContainer;
import graphql.language.Field;
import graphql.language.FragmentSpread;
import graphql.language.InlineFragment;
import graphql.language.Node;
import graphql.language.NodeVisitorStub;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;

public abstract class NodeVisitorPrefixesBase
    extends NodeVisitorStub
{
    /** Return the prefix map for this node - never null. */
    public static PrefixMap getEffectivePrefixMap(TraverserContext<Node> context) {
        PrefixMap result = context.getVar(PrefixMap2.class);
        if (result == null) {
            result = context.getVar(PrefixMap.class);
            if (result == null) {
                result = getParentPrefixMapOrNull(context);
                if (result == null) {
                    result = PrefixMapFactory.emptyPrefixMap();
                }
            }
        }
        return result;
    }

    public static PrefixMap getParentPrefixMapOrNull(TraverserContext<Node> context) {
        PrefixMap parentPrefixMap = context.getVarFromParents(PrefixMap2.class);
        if (parentPrefixMap == null) {
            parentPrefixMap = context.getVarFromParents(PrefixMap.class);
        }
        return parentPrefixMap;
    }

    protected void processPrefixes(DirectivesContainer<?> node, TraverserContext<Node> context) {
        PrefixMap parentPrefixMap = getParentPrefixMapOrNull(context);

        List<Directive> prefixDirectives = node.getDirectives("prefix");
        if (prefixDirectives != null) {
            PrefixMap prefixMap = PrefixMapFactory.create();
            for (Directive directive : prefixDirectives) {
                JenaGraphQlUtils.readPrefixDirective(directive, prefixMap);
            }
            context.setVar(PrefixMap.class, prefixMap);

            if (parentPrefixMap != null) {
                context.setVar(PrefixMap2.class, new PrefixMap2(parentPrefixMap, prefixMap));
            }
        }
    }

    @Override
    public final TraversalControl visitField(Field field, TraverserContext<Node> context) {
        processPrefixes(field, context);
        return visitFieldActual(field, context);
    }

    @Override
    public final TraversalControl visitInlineFragment(InlineFragment node, TraverserContext<Node> context) {
        processPrefixes(node, context);
        return visitInlineFragmentActual(node, context);
    }

    @Override
    public TraversalControl visitFragmentSpread(FragmentSpread node, TraverserContext<Node> context) {
        processPrefixes(node, context);
        return visitFragmentSpreadActual(node, context);
    }

    public abstract TraversalControl visitFieldActual(Field field, TraverserContext<Node> context);
    public abstract TraversalControl visitInlineFragmentActual(InlineFragment node, TraverserContext<Node> context);
    public abstract TraversalControl visitFragmentSpreadActual(FragmentSpread node, TraverserContext<Node> context);
}
