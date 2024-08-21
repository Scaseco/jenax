package org.aksw.jenax.graphql.sparql.v2.acc.state.api;

import java.util.List;

public class PathGon<K> {
    public static record Step<K>(K k, int index) {
        Step(K key) {
            this(key, -1);
        }
        Step(int index) {
            this(null, index);
        }
        boolean isKeyStep() {
            return k != null;
        }
    }
    protected List<Step<K>> steps;

    public PathGon(List<Step<K>> steps) {
        super();
        this.steps = steps;
    }
}
