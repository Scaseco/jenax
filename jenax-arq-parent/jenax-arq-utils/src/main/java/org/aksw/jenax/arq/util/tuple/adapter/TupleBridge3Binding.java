package org.aksw.jenax.arq.util.tuple.adapter;

import org.aksw.commons.tuple.bridge.TupleBridge3;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;

public class TupleBridge3Binding
    implements TupleBridge3<Binding, Node>
{
    private final Var vs;
    private final Var vp;
    private final Var vo;

    protected TupleBridge3Binding(Var vs, Var vp, Var vo) {
        super();
        this.vs = vs;
        this.vp = vp;
        this.vo = vo;
    }

    public static TupleBridge3Binding of(Var vs, Var vp, Var vo) {
        return new TupleBridge3Binding(vs, vp, vo);
    }

    @Override
    public Node get(Binding tupleLike, int componentIdx) {
        Node result = switch (componentIdx) {
        case 0 -> tupleLike.get(vs);
        case 1 -> tupleLike.get(vp);
        case 2 -> tupleLike.get(vo);
        default -> throw new IndexOutOfBoundsException(componentIdx);
        };
        return result;
    }

    @Override
    public Binding build(Node s, Node p, Node o) {
        return BindingFactory.binding(vs, s, vp, p, vo, o);
    }
}
