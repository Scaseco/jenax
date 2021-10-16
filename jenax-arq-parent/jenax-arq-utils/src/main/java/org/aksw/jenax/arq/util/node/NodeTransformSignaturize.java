package org.aksw.jenax.arq.util.node;

import java.util.Map;

import org.aksw.jenax.arq.util.var.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.graph.NodeTransform;

/**
 * Subsitute any variable with the name variable symbol.
 * Hence two objects with the same signature <i>might</i> be isomorphic.
 */
public class NodeTransformSignaturize
    implements NodeTransform
{
    protected NodeTransform baseTransform;
    protected Node placeholder;

    protected NodeTransformSignaturize(NodeTransform baseTransform,
            Node placeholder) {
        super();
        this.baseTransform = baseTransform;
        this.placeholder = placeholder;
    }

    public static NodeTransform create() {
        return create((node) -> null);
    }

    public static NodeTransform create(NodeTransform baseTransform) {
        return create(baseTransform, Vars.signaturePlaceholder);
    }

    public static NodeTransform create(NodeTransform baseTransform, Node placeholder) {
        return new NodeTransformSignaturize(baseTransform, placeholder);
    }

    public static NodeTransform create(Map<? extends Node, ? extends Node> nodeMap) {
        NodeTransform baseTransform = (node) -> nodeMap.get(node);//new NodeTransformRenameMap(nodeMap);
        NodeTransform result = NodeTransformSignaturize.create(baseTransform);
        return result;
    }



    //public static NodeTransform createSignaturizeTransform(Expr expr, Map<? extends Node, ? extends Node> nodeMap) {

    @Override
    public Node apply(Node node) {
        Node remap = baseTransform.apply(node);

        Node result = remap == null// || remap == node
                        ? (node.isVariable() ? placeholder : node)
                        : remap
                        ;

       return result;
    }

}
