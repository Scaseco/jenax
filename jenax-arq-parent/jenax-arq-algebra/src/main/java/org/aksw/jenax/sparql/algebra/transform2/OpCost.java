package org.aksw.jenax.sparql.algebra.transform2;

import org.apache.jena.sparql.algebra.Op;

public class OpCost {
    protected Op op;
    protected float cost;

    public OpCost(Op op, float cost) {
        super();
        this.op = op;
        this.cost = cost;
    }

    public Op getOp() {
        return op;
    }

    public float getCost() {
        return cost;
    }
}
