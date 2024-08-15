package org.aksw.jenax.facete.treequery2.api;

public interface InlineFragment
    extends Selection
{
    @Override
    default boolean isInlineFragment() {
        return true;
    }

    @Override
    default InlineFragment asInlineFragment() {
        return this;
    }
}
