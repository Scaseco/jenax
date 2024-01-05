package org.aksw.jenax.graphql.sparql;

/** Base class for directives whose scope can be only the current node or all of its children  */
public class Scope {
    protected boolean cascade;
    protected boolean self;

    public Scope(boolean cascade, boolean self) {
        super();
        this.cascade = cascade;
        this.self = self;
    }

    public boolean isCascade() {
        return cascade;
    }

    public boolean isSelf() {
        return self;
    }
}
