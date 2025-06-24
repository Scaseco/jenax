package org.aksw.jenax.graphql.sparql.v2.gon.meta;

/**
 * Enum to capture what types of parents are valid for a given gon element.
 *
 * <ul>
 *   <li>
 *     NonObject category members:
 *     <ul>
 *       <li>Array (parent must be NonObject)</li>
 *       <li>Entry (parent must be Object)</li>
 *       <li>Root (does not have a parent)</li>
 *     </ul>
 *   </li>
 *   <li>
 *     Node category members: (parent must be of NonObject)
 *     <ul>
 *       <li>Literal</li>
 *       <li>Object</li>
 *       <li>Array</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * Note, that Array exists in both categories.
 * The non-object category is relevant to describe the set of valid parent types of a gon type.
 *
 * Node category is for constructs that "can exist by themselves" (in contrast to an entry
 * which can't exist without an enclosing object).
 */
public enum GonType {
    ARRAY   (RawGonType.ARRAY,   GonCategory.NODE_TYPE, GonCategory.NON_OBJECT),
    ENTRY   (RawGonType.ENTRY,   GonCategory.NON_OBJECT, GonCategory.OBJECT),
    ROOT    (RawGonType.ROOT,    GonCategory.NON_OBJECT, GonCategory.ROOT),

    LITERAL (RawGonType.LITERAL, GonCategory.NODE_TYPE,  GonCategory.NON_OBJECT),
    OBJECT  (RawGonType.OBJECT,  GonCategory.NODE_TYPE,  GonCategory.NON_OBJECT),

    // Unknown can be anything and the parent can be anything
    UNKNOWN (RawGonType.UNKNOWN, GonCategory.UNKNOWN,    GonCategory.UNKNOWN),
    ;

    private final RawGonType rawType;
    private final GonCategory category;
    private final GonCategory validParentCategory;

    private GonType(RawGonType rawType, GonCategory category, GonCategory validParentCategory) {
        this.rawType = rawType;
        this.category = category;
        this.validParentCategory = validParentCategory;
    }

    public RawGonType getRawType() {
        return rawType;
    }

    /**
     * Convenience method. True iff this' category is NODE_TYPE,
     * which stands for literal, object or array.
     */
    public boolean isNodeType() {
        return GonCategory.NODE_TYPE.equals(category);
    }

    public GonCategory getCategory() {
        return category;
    }

    public GonCategory getValidParentCategory() {
        return validParentCategory;
    }

    /** The given type must be in this type's valid parent category. */
    public boolean isValidChildOf(GonType parentType) {
        // XXX Add unknown to all categories?
        boolean result = (validParentCategory.getMembers().contains(parentType.getRawType())
                || UNKNOWN.equals(parentType));

        return result;
    }
}
