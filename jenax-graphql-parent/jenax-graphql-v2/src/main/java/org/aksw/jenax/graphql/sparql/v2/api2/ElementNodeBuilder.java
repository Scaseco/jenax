package org.aksw.jenax.graphql.sparql.v2.api2;

/**
 * Builder for fields. Building fields is a two stage process where
 * first the field's data is configured,
 * and then the members can be added.
 * Calling {@link #build()} on a field builder returns a {@link ElementMemberBuilder}.
 */
//public class ElementNodeBuilder
//    implements HasConnectiveBuilder<ElementNodeBuilder>, HasParentVarBuilder<ElementNodeBuilder>, Cloneable
//{
//    protected String label;
//    // protected String name;
//
//    /** The variables of the parent pattern on which to join.
//     *  If this field is omitted then the parent declared target variables are used.
//     */
//    protected List<Var> parentVars;
//
//    /** The original connective. */
//    protected Connective connective;
//
//    // protected List<ElementNode> children;
//    // protected Map<String, Selection> childrenByName = new LinkedHashMap<>();
//
//    // protected List<FieldBuilder> subFieldBuilders;
//
//    @Override
//    public ElementNodeBuilder clone() {
//        return new ElementNodeBuilder()
//                .label(label)
//                // .name(name)
//                .parentVars(parentVars)
//                .connective(connective)
//                ;
//    }
//
//    @Override
//    public void setParentVars(List<Var> vars) {
//        this.parentVars = vars;
//    }
////    protected List<Selection> getOrSetSelections() {
////        if (subSelections == null) {
////            subSelections = new ArrayList<>();
////        }
////        return subSelections;
////    }
//
//    public Connective getConnective() {
//        return connective;
//    }
//
//    /** Fixed name. If a field with that already exists and exception will be raised. */
//    public ElementNodeBuilder name(String name) {
//        this.name = name;
//        return this;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    /** Name for auto-allocation. */
//    public ElementNodeBuilder baseName(String baseName) {
//        this.baseName = baseName;
//        return this;
//    }
//
//    @Override
//    public ElementNodeBuilder connective(Connective connective) {
//        this.connective = connective;
//        return this;
//    }
//
//    // @Override
////    public ElementNodeBuilder addChild(Selection child) {
////        String name = child.getName();
////        if (childrenByName.containsKey(name)) {
////            throw new IllegalArgumentException("There already exists a child with name: " + name);
////        }
////        this.childrenByName.put(child.getName(), child);
////        return this;
////    }
////
////    public ElementNodeBuilder setChildren(Collection<? extends Selection> children) {
////        for (Selection child : children) {
////            addChild(child);
////        }
////        return this;
////    }
//
//    public ElementNode build() {
//        Objects.requireNonNull(connective);
//
//        Map<String, Selection> finalChildren = new LinkedHashMap<>();
//        return new ElementNode(name, parentVars, connective, finalChildren);
//    }
//
//    /** Obtains the string from a field built from this builder's current state. */
//    @Override
//    public String toString() {
//        ElementNode field = build();
//        String result = field.toString();
//        return result;
//    }
//}
