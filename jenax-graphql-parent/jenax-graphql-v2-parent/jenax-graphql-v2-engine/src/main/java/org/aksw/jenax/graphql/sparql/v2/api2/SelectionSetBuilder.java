package org.aksw.jenax.graphql.sparql.v2.api2;

/**
 * The selection set builder can adjust and auto-generate names on the supplied selection builders.
 *
 * @param <X>
 */
//public abstract class SelectionSetBuilder<X extends SelectionSetBuilder<X>>
//    extends BasicConnectInfo
//    implements HasFieldBuilder, HasSelf<X>
//{
//
//    /** Children can be modified. */
//    protected Map<String, SelectionBuilder> selections;
//    // protected Map<String, Selection> selections;
//
//    public Set<String> getSelectionNames() {
//        return selections == null ? Collections.emptySet() : selections.keySet();
//    }
//
//    public Collection<SelectionBuilder> getSelections() {
//        return selections == null ? Collections.emptySet() : selections.values();
//    }
//
//    public Map<String, SelectionBuilder> getSelectionMap() {
//        return selections == null ? Collections.emptyMap() : selections;
//    }
//
//    Map<String, SelectionBuilder> getOrSetSelections() {
//        if (selections == null) {
//            selections = new LinkedHashMap<>();
//        }
//        return selections;
//    }
//
////    public Collection<Selection> getSelections() {
////        return selections == null ? Collections.emptySet() : selections.values();
////    }
////
////    public Map<String, Selection> getSelectionMap() {
////        return selections == null ? Collections.emptyMap() : selections;
////    }
////
////    Map<String, Selection> getOrSetSelections() {
////        if (selections == null) {
////            selections = new LinkedHashMap<>();
////        }
////        return selections;
////    }
//
//
//    public X addSelection(SelectionBuilder selectionBuilder) {
//        String name = selectionBuilder.getName();
//        Map<String, SelectionBuilder> map = getOrSetSelections();
//
//        String finalName = name;
//        if (finalName == null) {
//            String baseName = selectionBuilder.getBaseName();
//            String finalBaseName = baseName == null ? "field" : baseName;
//            finalName = StringUtils.allocateName(finalBaseName + map.size(), true, map::containsKey);
//        }
//
//        if (map.containsKey(finalName)) {
//            throw new IllegalArgumentException("Duplicate selection with name " + finalName);
//        }
//
//        List<Var> finalParentVars = null;
//        List<Var> parentVars = selectionBuilder.getParentVars();
//
//        finalParentVars = parentVars != null ? parentVars : this.getDefaultTargetVars();
//        Validation.validateParentVars(this, finalParentVars);
//
//        Connective connective = selectionBuilder.getConnective();
//        List<Var> connectVars = connective.getConnectVars();
//        if (finalParentVars.size() != connectVars.size()) {
//            throw new RuntimeException("parentVars.length != connectVars.length: Cannot connect parent." + finalParentVars + " to this." + connective.getConnectVars());
//        }
//
////            if (finalName == null) {
////                String finalBaseName = baseName == null ? "field" : baseName;
////                finalName = StringUtils.allocateName(finalBaseName + target.getSelections().size(), true, target.getSelectionNames()::contains);
////            }
//
//        // FieldLikeBuilder selection = selectionBuilder.clone(finalName, finalParentVars);
//
//        // Selection selection = selectionBuilder.clone(finalName, finalParentVars).build();
//        map.put(finalName, selectionBuilder);
//        return self();
//    }
//
//    public SelectionSetBuilder(List<Var> defaultTargetVars, Set<Var> visibleVars) { //, List<Selection> selections) {
//        super(defaultTargetVars, visibleVars);
//        // this.selections = Objects.requireNonNull(selections);
//    }
//
//    /**
//     * Return a builder for a fields on this connective.
//     * Calling {@link ElementNodeBuilder#build()} returns the new field and also adds it to this connective's field list.
//     */
//    @Override
//    public ElementNodeBuilder newFieldBuilder() {
//        return new ElementNodeBuilder(this);
//    }
//
//    public FragmentSpreadBuilder<?> newFragmentSpreadBuilder() {
//        return new FragmentSpreadBuilder<>(this);
//    }
//}
