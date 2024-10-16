package org.aksw.jenax.facete.treequery2.api;

public interface Selection {
    NodeQuery getParent();

    default boolean isInlineFragment() {
        return false;
    }

    default InlineFragment asInlineFragment() {
        throw new UnsupportedOperationException("not a fragment");
    }

    default boolean isNodeQuery() {
        return false;
    }

    default NodeQuery asNodeQuery() {
        throw new UnsupportedOperationException("not a fragment");
    }
}
