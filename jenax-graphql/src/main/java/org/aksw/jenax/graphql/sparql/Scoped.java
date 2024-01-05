package org.aksw.jenax.graphql.sparql;

/** Base class for directives whose scope can be only the current node or all of its children  */
public class Scoped {
    protected boolean all;

    public Scoped(boolean all) {
        super();
        this.all = all;
    }

    public boolean isAll() {
        return all;
    }
}
