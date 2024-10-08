package org.aksw.jenax.graphql.sparql.v2.ron;

import org.apache.jena.sparql.path.P_Path0;

public class ParentLinkObjectImpl
    implements ParentLinkObject
{
    private final RdfObject parent;
    private final P_Path0 key;

    public ParentLinkObjectImpl(RdfObject parent, P_Path0 key) {
        super();
        this.parent = parent;
        this.key = key;
    }

    @Override
    public RdfObject getParent() {
        return parent;
    }

    @Override
    public P_Path0 getKey() {
        return key;
    }
}
