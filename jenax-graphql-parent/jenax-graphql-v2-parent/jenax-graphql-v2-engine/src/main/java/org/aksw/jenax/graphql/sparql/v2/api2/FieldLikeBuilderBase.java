package org.aksw.jenax.graphql.sparql.v2.api2;

/**
 * Builder for fields. Building fields is a two stage process where
 * first the field's data is configured,
 * and then the members can be added.
 * Calling {@link #build()} on a field builder returns a {@link ElementMemberBuilder}.
 *
 * @param T The sub-type if itself
 * @param P The parent to which to attach this field like
 */
//public class FieldLikeBuilderBase<T extends FieldLikeBuilderBase<T>>
//    implements HasSelf<T>
//{
//    public class ConnectiveSubBuilder
//        extends ConnectiveBuilder<ConnectiveSubBuilder> {
//
//        @Override
//        public Connective build() {
//            throw new IllegalStateException("Use .set() instead of .build() on sub builders.");
//        }
//
//        public T set() {
//            Connective tmp = super.build();
//            FieldLikeBuilderBase.this.connective(tmp);
//            return FieldLikeBuilderBase.this.self();
//        }
//    }
//
//    // TODO target needs to expose variables and accept instances built by this class
//    protected SelectionSetBuilder target;
//
//    protected String baseName;
//    protected String name;
//
//    /** The variables of the parent pattern on which to join.
//     *  If this field is omitted then the parent declared target variables are used.
//     */
//    protected List<Var> parentVars;
//
//    /** The original connective. */
//    protected Connective connective;
//
//    // protected List<Selection> subSelections;
//
//    public FieldLikeBuilderBase(SelectionSetBuilder target) {
//        super();
//        this.target = target;
//    }
//
////    protected List<Selection> getOrSetSelections() {
////        if (subSelections == null) {
////            subSelections = new ArrayList<>();
////        }
////        return subSelections;
////    }
//
//    /** Fixed name. If a field with that already exists and exception will be raised. */
//    public T name(String name) {
//        this.name = name;
//        return self();
//    }
//
//    /** Name for auto-allocation. */
//    public T baseName(String baseName) {
//        this.baseName = baseName;
//        return self();
//    }
//
//    /**
//     * Return a builder that upon calling {@link ConnectiveSubBuilder#set()} sets
//     * the built connective on this field builder.
//     */
//    public ConnectiveSubBuilder newConnectiveBuilder() {
//        return new ConnectiveSubBuilder();
//    }
//
//    public T parentVarNames(String...varNames) {
//        List<String> list = Arrays.asList(varNames);
//        parentVarNames(list);
//        return self();
//    }
//
//    public T parentVarNames(List<String> varNames) {
//        List<Var> list = Var.varList(varNames);
//        parentVars(list);
//        return self();
//    }
//
//    public T parentVars(Var ... vars) {
//        List<Var> list = Arrays.asList(vars);
//        parentVars(list);
//        return self();
//    }
//
//    public T parentVars(List<Var> varNames) {
//        Validation.validateParentVars(target, varNames);
//        this.parentVars = new ArrayList<>(varNames);
//        return self();
//    }
//
//    public T connective(Connective connective) {
//        this.connective = connective;
//        return self();
//    }
//
////    public FieldBuilder newSubFieldBuilder() {
////        return new FieldBuilder(this);
////    }
//
//    public ElementMemberBuilder build() {
//        Objects.requireNonNull(connective);
//
//        List<Var> finalParentVars = null;
//        String finalName = name;
//        if (target != null) {
//            finalParentVars = parentVars != null ? parentVars : target.getDefaultTargetVars();
//            Validation.validateParentVars(target, finalParentVars);
//
//            List<Var> connectVars = connective.getConnectVars();
//            if (finalParentVars.size() != connectVars.size()) {
//                throw new RuntimeException("parentVars.length != connectVars.length: Cannot connect parent." + finalParentVars + " to this." + connective.getConnectVars());
//            }
//
//            if (finalName == null) {
//                String finalBaseName = baseName == null ? "field" : baseName;
//                finalName = StringUtils.allocateName(finalBaseName + target.getSelections().size(), true, target.getSelectionNames()::contains);
//            }
//        }
//
//        if (finalName == null) {
//             finalName = "root";
//        }
//
//        ElementMemberBuilder field = new ElementMemberBuilder(null, finalName, baseName, finalParentVars, connective);
//
//        if (target != null) {
//            // target.getOrSetSelections().add(field);
//            target.addSelection(field);
//        }
//        return field;
//    }
//
//    /** Obtains the string from a field built from this builder's current state. */
//    @Override
//    public String toString() {
//        ElementMemberBuilder fieldMemberBuilder = build();
//        ElementNode field = fieldMemberBuilder.build();
//        String result = field.toString();
//        return result;
//    }
//}
