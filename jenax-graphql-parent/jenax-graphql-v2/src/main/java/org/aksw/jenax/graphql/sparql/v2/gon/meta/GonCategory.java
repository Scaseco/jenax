package org.aksw.jenax.graphql.sparql.v2.gon.meta;

import java.util.EnumSet;

public enum GonCategory {
    NON_OBJECT(EnumSet.of(RawGonType.ARRAY, RawGonType.ENTRY, RawGonType.ROOT)),
    NODE_TYPE (EnumSet.of(RawGonType.LITERAL, RawGonType.OBJECT)),
    OBJECT    (EnumSet.of(RawGonType.OBJECT)), // The object and node type categories contain OBJECT
    ROOT      (EnumSet.of(RawGonType.ROOT)),

    // Allow everything for unknown
    UNKNOWN   (EnumSet.allOf(RawGonType.class)),
    ;

    private final EnumSet<RawGonType> members;

    private GonCategory(EnumSet<RawGonType> members) {
        this.members = members;
    }

    public EnumSet<RawGonType> getMembers() {
        return members;
    }
}
