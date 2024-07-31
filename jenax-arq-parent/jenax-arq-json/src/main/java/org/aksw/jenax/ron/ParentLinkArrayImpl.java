package org.aksw.jenax.ron;

public class ParentLinkArrayImpl
    implements ParentLinkArray
{
    private final RdfArray parent;
    private final int index;

    public ParentLinkArrayImpl(RdfArray parent, int index) {
        super();
        this.parent = parent;
        this.index = index;
    }

    @Override
    public RdfArray getParent() {
        return parent;
    }

    @Override
    public int getIndex() {
        return index;
    }
}
