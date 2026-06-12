package org.apache.jena.tdb2.solver;

import java.util.Comparator;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.join.JoinKey;
import org.apache.jena.tdb2.store.NodeId;

/**
 * Compare BindingNodeId instances by a join key.
 */
public class BindingNodeIdComparator
    implements Comparator<BindingNodeId>
{
    private final JoinKey joinKey;
    private Comparator<NodeId> nodeIdComparator;

    public BindingNodeIdComparator(JoinKey joinKey, Comparator<NodeId> nodeIdComparator) {
        super();
        this.joinKey = joinKey;
        this.nodeIdComparator = nodeIdComparator;
    }

    @Override
    public int compare(BindingNodeId a, BindingNodeId b) {
        int result = compare(joinKey, a, b, nodeIdComparator);
        return result;
    }

    /** Raises NPE if a binding maps the variable to null! */
    public static BindingNodeIdComparator of(JoinKey joinKey) {
        return new BindingNodeIdComparator(joinKey, NodeId::compare);
    }

    public static int compare(JoinKey joinKey, BindingNodeId a, BindingNodeId b) {
        int r = compare(joinKey, a, b, NodeId::compare);
        return r;
    }

    public static int compare(JoinKey joinKey, BindingNodeId a, BindingNodeId b, Comparator<NodeId> nodeIdComparator) {
        int r = 0;
        for (Var v : joinKey) {
            NodeId na = a.get(v);
            NodeId nb = b.get(v);
            r = nodeIdComparator.compare(na, nb);
            if (r != 0) {
                break;
            }
        }
        return r;
    }
}
