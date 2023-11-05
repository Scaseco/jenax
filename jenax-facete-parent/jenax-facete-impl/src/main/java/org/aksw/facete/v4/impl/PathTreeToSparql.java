package org.aksw.facete.v4.impl;

// Superseded by ElementGenerator
//public class PathTreeToSparql {
//
//    public static MappedQuery createQuery(UnaryRelation baseConcept, TreeData<FacetPath> treeData, Predicate<FacetPath> isProjected) {
//
//        Generator<Var> varGen = GeneratorFromFunction.createInt().map(i -> Var.alloc("vv" + i));
//
//        Var rootVar = baseConcept.getVar();
//        DynamicInjectiveFunction<FacetPath, Var> ifn = DynamicInjectiveFunction.of(varGen);
//        ifn.getMap().put(FacetPathOps.get().newRoot(), rootVar);
//        // Var rootVar = ifn.apply(PathOpsPPA.get().newRoot());
//
//        ElementGroup group = new ElementGroup();
//        baseConcept.getElements().forEach(group::addElement);
//        for (FacetPath rootPath : treeData.getRootItems()) {
//            Element elt = accumulate(rootVar, rootPath, ifn, treeData::getChildren);
//            ElementUtils.toElementList(elt).forEach(group::addElement);
//            // group.addElement(elt);
//        }
//        Element elt = group.size() == 1 ? group.get(0) : group;
//
//        List<Var> visibleVars = ifn.getMap().entrySet().stream()
//                .filter(e -> isProjected.test(e.getKey()))
//                .map(Entry::getValue)
//                .collect(Collectors.toList());
//
//        Query query = new Query();
//        query.setQuerySelectType();
//        query.setQueryPattern(elt);
//        query.addProjectVars(visibleVars);
//
//        System.err.println("Generated Query: " + query);
//        MappedQuery result = new MappedQuery(treeData, query, ifn.getMap().inverse());
//
//        return result;
//    }
//
////    public static Triple stepToTriple(AliasedStep step) {
////
////    }
//
//    public static Element accumulate(
//            Var parentVar,
//            FacetPath path,
//            Function<FacetPath, Var> pathToVar,
//            Function<FacetPath, ? extends Iterable<FacetPath>> getChildren) {
//        Element result;
//
//        Element coreElt = null;
//        Var targetVar = pathToVar.apply(path);
//        Node secondaryNode;
//        if (!path.getSegments().isEmpty()) {
//            FacetStep step = path.getFileName().toSegment();
//            Node predicateNode = step.getNode();
//            boolean isFwd = step.isForward();
//            Integer c = step.getTargetComponent();
//
//            if (NodeUtils.ANY_IRI.equals(predicateNode)) { // predicateNode.isVariable()) { // TODO Use a special IRI constant "ANY" to refer to any property?
//                Integer toggledComponent = FacetStep.isTarget(c) ? FacetStep.PREDICATE : FacetStep.TARGET;
//                FacetStep s = step.copyStep(toggledComponent);
//            // FacetStep toggledTarget = step.toggleTarget();
//                secondaryNode = pathToVar.apply(path.resolveSibling(s));
//            } else {
//                secondaryNode = predicateNode;
//            }
//
//            if (FacetStep.isTarget(c)) {
//                coreElt = ElementUtils.createElementTriple(parentVar, secondaryNode, targetVar, isFwd);
//            } else {
//                coreElt = ElementUtils.createElementTriple(parentVar, targetVar, secondaryNode, isFwd);
//            }
//        }
//
//        Iterable<FacetPath> children = getChildren.apply(path);
//        if (children != null && children.iterator().hasNext()) {
//            ElementGroup eltGrp = new ElementGroup();
//            if (coreElt != null) {
//                eltGrp.addElement(coreElt);
//            }
//
//            for (FacetPath subPath : children) {
//                Element subElt = accumulate(targetVar, subPath, pathToVar, getChildren);
//                ElementOptional optElt = new ElementOptional(subElt);
//                eltGrp.addElement(optElt);
//            }
//            result = eltGrp.size() == 1 ? eltGrp.get(0) : eltGrp;
//        } else {
//            result = coreElt == null ? new ElementGroup() : coreElt;
//        }
//
//        return result;
//    }
//}
