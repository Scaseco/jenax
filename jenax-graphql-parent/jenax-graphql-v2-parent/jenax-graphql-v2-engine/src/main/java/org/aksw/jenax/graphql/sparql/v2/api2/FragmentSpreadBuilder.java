package org.aksw.jenax.graphql.sparql.v2.api2;

//public class FragmentSpreadBuilder<T extends FragmentSpreadBuilder<T>> {
//
//    /** The node on which to add the field. */
//    protected final SelectionSetBuilder target;
//
//    protected String baseName;
//    protected String name;
//    protected Fragment fragment;
//
//    /** Variable mapping that defines for each declared variable of the fragment a corresponding one in the connective. */
//    protected Map<Var, Var> fragmentToInput = new LinkedHashMap<>();
//
//    public FragmentSpreadBuilder(SelectionSetBuilder target) {
//        super();
//        this.target = target;
//    }
//
//    @SuppressWarnings("unchecked")
//    protected T self() {
//        return (T)this;
//    }
//
//    public T baseName(String baseName) {
//        this.baseName = baseName;
//        return self();
//    }
//
//    public T name(String name) {
//        this.name = name;
//        return self();
//    }
//
//    public T setFragment(Fragment fragment) {
//        this.fragment = fragment;
//        return self();
//    }
//
//    public T map(String here, String parent) {
//        Var from = Var.alloc(here);
//        Var to = Var.alloc(parent);
//        map(from, to);
//        return self();
//    }
//
//    public T map(Var here, Var parent) {
//        fragmentToInput.put(here, parent);
//        return self();
//    }
//
//    public FragmentSpread build() {
//        Objects.requireNonNull(fragment);
//        if (fragmentToInput.isEmpty()) {
//            throw new RuntimeException("No var mappings provided");
//        }
//
//        Map<Var, Var> tmp = new LinkedHashMap<>(fragmentToInput);
//
//        String finalName = name;
//        if (finalName == null) {
//            String finalBaseName = baseName == null ? "field" : baseName;
//            finalName = StringUtils.allocateName(finalBaseName + target.getSelections().size(), true, target.getSelectionNames()::contains);
//        }
//
//        Validation.validateParentVars(target, fragmentToInput.values());
//
//        FragmentSpread result = new FragmentSpread(target, finalName, fragment, tmp);
//
//        target.addSelection(result);
//        return result;
//
//    }
//}
