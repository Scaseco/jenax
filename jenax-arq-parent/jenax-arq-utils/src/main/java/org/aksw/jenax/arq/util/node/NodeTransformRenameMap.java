package org.aksw.jenax.arq.util.node;

import java.util.Map;
import java.util.Objects;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.graph.NodeTransform;

public class NodeTransformRenameMap
    implements NodeTransform {

    private final Map<?, ? extends Node> map;

    protected NodeTransformRenameMap(Map<?, ? extends Node> map)
    {
        this.map = Objects.requireNonNull(map);
    }

    public static NodeTransformRenameMap create(Map<?, ? extends Node> map) {
        return new NodeTransformRenameMap(map);
    }

    public final Node apply(Node node)
    {
        Node result = map.get(node);
        if (result == null) {
            result = node;
        }

        return result;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((map == null) ? 0 : map.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NodeTransformRenameMap other = (NodeTransformRenameMap) obj;
        if (map == null) {
            if (other.map != null)
                return false;
        } else if (!map.equals(other.map))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "NodeTransformRenameMap [map=" + map + "]";
    }
}