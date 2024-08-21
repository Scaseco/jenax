package org.aksw.jenax.graphql.sparql.v2.api2;

/**
 * A field links a child connective to a parent selection set.
 *
 * Selection sets are Fragments and Connectives.
 */
//public class ElementMemberBuilder
//    extends SelectionSetBuilder<ElementMemberBuilder>
//    implements SelectionBuilder
//{
//    protected final String name;
//    protected final String baseName;
//
//    // protected final SelectionSet parent;
//    protected final Connective connective;
//
//    // XXX Could extend to "List<Expr> parentExprs" where the expressions are computed over the parent element.
//    //     This way we could generate BIND blocks on the parent block which are then used for LATERAL joins.
//    protected final List<Var> parentVars;
//
//    /** Map effective vars to the original ones. */
//    // protected final Map<Var, Var> effectiveToOriginal;
//
//    /** The effective connective whose variables join with the parent. */
//    // protected final Connective effectiveConnective;
//
//    public ElementMemberBuilder(SelectionSetBuilder parent, String name, String baseName, List<Var> parentVars, Connective connective) { //Map<Var, Var> effectiveToOriginal, Connective effectiveConnective) {
//        super(connective.getDefaultTargetVars(), connective.visibleVars);
//        //super(parent);
//        //this.parent = parent;
//        this.name = name;
//        this.baseName = baseName;
//        this.parentVars = parentVars;
//        this.connective = connective;
//
////        this.effectiveToOriginal = effectiveToOriginal;
////        this.effectiveConnective = effectiveConnective;
//    }
//
//    // @Override
//    public SelectionBuilder clone() {
//        return new ElementMemberBuilder(null, name, baseName, parentVars, connective);
//    }
//
//    @Override
//    public SelectionBuilder clone(String finalName, List<Var> parentVars) {
//        ElementMemberBuilder result = new ElementMemberBuilder(null, finalName, baseName, parentVars, connective);
//        // getSelections().forEach(selection -> selection.toBuilder(result));
//        // getSelections().forEach(selection -> result.addSelection(selection.clone(selection.getName(), selection.getParentVars())));
//
//        return result;
//    }
//
////    @Override
////    public FieldLikeBuilder setName(String name) {
////        this.name = name;
////        return this;
////    }
//
//    @Override
//    public String getName() {
//        return name;
//    }
//
//    @Override
//    public String getBaseName() {
//        return baseName;
//    }
//
////    @Override
////    public SelectionSet getParent() {
////        return parent;
////    }
//
//    public Connective getConnective() {
//        return connective;
//    }
//
//
//    @Override
//    public List<Var> getParentVars() {
//        return parentVars;
//    }
////
////    public FieldMemberBuilder addSelection(FieldMemberBuilder selectionBuilder) {
////        String name = selectionBuilder.getName();
////        Map<String, Selection> map = getOrSetSelections();
////
////        String finalName = name;
////        if (finalName == null) {
////            String baseName = selectionBuilder.getBaseName();
////            String finalBaseName = baseName == null ? "field" : baseName;
////            finalName = StringUtils.allocateName(finalBaseName + map.size(), true, map::containsKey);
////        }
////
//////        if (finalName == null) {
//////           finalName = "root";
//////        }
////
////        if (map.containsKey(finalName)) {
////            throw new IllegalArgumentException("Duplicate selection with name " + finalName);
////        }
////
////        Selection selection = selectionBuilder.clone().setName(finalName).build();
////        map.put(finalName, selection);
////        return self();
////    }
//
//    //d(SelectionSet parentConnective, List<Var> parentVars, Connective connective)
//
////    @Override
////    public <T> T accept(ConnectiveVisitor<T> visitor) {
////        T result = visitor.visit(this);
////        return result;
////    }
//
////    @Override
////    public final String toString() {
////        String result = ConnectiveVisitorToString.toString(this);
////        return result;
////    }
//
//    @Override
//    public ElementNode build() {
//        Map<String, Selection> finalSelections = getSelectionMap().entrySet().stream().collect(Collectors.toMap(
//            e -> e.getKey(),
//            e -> e.getValue().build()
//        ));
//
//        // Map<String, Selection> finalSelections = getSelectionMap();
//
//        String finalName = name == null ? "root" : name;
//
//        return new ElementNode(null, finalName, parentVars, connective, finalSelections);
//    }
//}

