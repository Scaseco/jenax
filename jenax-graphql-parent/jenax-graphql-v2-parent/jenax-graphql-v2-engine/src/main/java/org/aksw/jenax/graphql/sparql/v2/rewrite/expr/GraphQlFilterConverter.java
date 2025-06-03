package org.aksw.jenax.graphql.sparql.v2.rewrite.expr;

import graphql.language.Argument;
import graphql.language.Node;
import graphql.language.NodeVisitorStub;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;

public class GraphQlFilterConverter
    extends NodeVisitorStub
{
    @Override
    public TraversalControl visitObjectField(ObjectField node, TraverserContext<Node> context) {
        node.getName();
        node.getValue();
        return super.visitObjectField(node, context);
    }

    @Override
    public TraversalControl visitObjectValue(ObjectValue node, TraverserContext<Node> context) {
        return super.visitObjectValue(node, context);
    }

    @Override
    public TraversalControl visitArgument(Argument node, TraverserContext<Node> context) {
        return super.visitArgument(node, context);
    }
}
