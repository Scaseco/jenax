package org.aksw.jenax.graphql.sparql.v2.api2;

//public class FragmentBuilder
//    implements HasTargetVarBuilder<FragmentBuilder>
//{
//    public static FragmentBuilder newBuilder() {
//        return new FragmentBuilder();
//    }
//
//    protected List<Var> defaultTargetVars;
//    protected Set<Var> visibleVars;
//
//    @Override
//    public void setTargetVars(List<Var> targetVars) {
//        this.defaultTargetVars = targetVars;
//    }
////    public FragmentBuilder(Set<Var> visibleVars) {
////        super();
////        this.visibleVars = visibleVars;
////    }
//
//    public FragmentBuilder visibleVarNames(String...varNames) {
//        List<String> list = Arrays.asList(varNames);
//        visibleVarNames(list);
//        return self();
//    }
//
//    public FragmentBuilder visibleVarNames(List<String> varNames) {
//        List<Var> list = Var.varList(varNames);
//        visibleVars(list);
//        return self();
//    }
//
//    public FragmentBuilder visibleVars(Var ... vars) {
//        List<Var> list = Arrays.asList(vars);
//        targetVars(list);
//        return self();
//    }
//
//    public FragmentBuilder visibleVars(List<Var> varNames) {
//        this.visibleVars = new LinkedHashSet<>(varNames);
//        return self();
//    }
//    public FragmentMemberBuilder build() {
////        Objects.requireNonNull(element);
////        Objects.requireNonNull(connectVars);
////
////        if (connectVars.isEmpty()) {
////            throw new RuntimeException("Connect vars was empty. Cannot connect an element without any connect vars.");
////        }
////
////        // Check for correct variable usage
////        Op op = Algebra.compile(element);
////        Set<Var> visibleVars = OpVars.visibleVars(op);
//
////        Set<Var> absentConnectVars = SetUtils.difference(new HashSet<>(connectVars), visibleVars);
////        if (!absentConnectVars.isEmpty()) {
////            throw new RuntimeException("The following connectVars are not present or visible in the pattern: " + element);
////        }
//
//        Set<Var> absentTargetVars = SetUtils.difference(new HashSet<>(defaultTargetVars), visibleVars);
//        if (!absentTargetVars.isEmpty()) {
//            throw new RuntimeException("The following targetVars are not present or visible in the pattern: ");
//        }
//
//        return new FragmentMemberBuilder(defaultTargetVars, visibleVars);
//    }
//}
