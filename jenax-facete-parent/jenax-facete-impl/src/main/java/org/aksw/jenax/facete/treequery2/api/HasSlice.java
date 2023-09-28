package org.aksw.jenax.facete.treequery2.api;

public interface HasSlice {
    Long offset();
    HasSlice offset(Long offset);

    Long limit();
    HasSlice limit(Long limit);
}
