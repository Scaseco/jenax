package org.aksw.jenax.io.json.gon;

public class ParentLinkArrayImpl<K, V>
    implements ParentLinkArray<K, V>
{
    private final GonArray<K, V> parent;
    private final int index;

    public ParentLinkArrayImpl(GonArray<K, V> parent, int index) {
        super();
        this.parent = parent;
        this.index = index;
    }

    @Override
    public GonArray<K, V> getParent() {
        return parent;
    }

    @Override
    public int getIndex() {
        return index;
    }
}
