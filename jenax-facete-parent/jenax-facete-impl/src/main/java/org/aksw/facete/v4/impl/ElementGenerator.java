package org.aksw.facete.v4.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aksw.commons.util.direction.Direction;
import org.aksw.facete.v3.api.FacetConstraints;
import org.aksw.facete.v3.api.NodeFacetPath;
import org.aksw.facete.v3.api.TreeData;
import org.aksw.facete.v3.api.TreeQueryNode;
import org.aksw.facete.v3.api.VarScope;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.concepts.TernaryRelationImpl;
import org.aksw.jenax.arq.util.node.NodeCustom;
import org.aksw.jenax.arq.util.node.NodeTransformLib2;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.facete.treequery2.api.ConstraintNode;
import org.aksw.jenax.facete.treequery2.api.FacetPathMapping;
import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.facete.treequery2.api.RelationQuery;
import org.aksw.jenax.facete.treequery2.api.ScopedFacetPath;
import org.aksw.jenax.facete.treequery2.impl.ElementGeneratorLateral;
import org.aksw.jenax.facete.treequery2.impl.FacetPathMappingImpl;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.relation.api.BinaryRelation;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.aksw.jenax.sparql.relation.api.TernaryRelation;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_NotOneOf;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementFilter;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.graph.Traverser;

/* This class should supersede {@link FacetedQueryGenerator}. */
public class ElementGenerator {

    protected Element baseElement;
    protected ScopedFacetPath focusPath;

    protected FacetPathMapping pathMapping;
    protected PropertyResolver propertyResolver = new PropertyResolverImpl();

    /** Hierarchy of all referenced paths. Serves as the basis for graph pattern creation. */
    protected TreeData<ScopedFacetPath> facetTree = new TreeData<>();

    // Should the value be a wrapper that conveniently links back to all mentioned paths?
    protected SetMultimap<ScopedFacetPath, Expr> constraintIndex;

    public ElementGenerator(FacetPathMapping pathMapping, SetMultimap<ScopedFacetPath, Expr> constraintIndex, ScopedFacetPath focusPath) {
        super();
        this.pathMapping = pathMapping;
        this.constraintIndex = constraintIndex;
        this.focusPath = focusPath;

        this.baseElement = null;
    }

    public void setBaseElement(Element baseElement) {
        this.baseElement = baseElement;
    }

    public Element getBaseElement() {
        return baseElement;
    }

    public FacetPathMapping getPathMapping() {
        return pathMapping;
    }

    public void addPath(ScopedFacetPath facetPath) {
        // facetTree.computeIfAbsent(facetPath.getScope(), sc -> new TreeData<>()).putItem(facetPath.getFacetPath(), FacetPath::getParent);
        facetTree.putItem(facetPath, ScopedFacetPath::getParent);
    }

    public void addExpr(Expr expr) {
        Set<ScopedFacetPath> paths = NodeCustom.mentionedValues(ScopedFacetPath.class, expr);
        for (ScopedFacetPath path : paths) {
            addPath(path);
            constraintIndex.put(path, expr);
        }
    }


    public static ElementGenerator configure(ConstraintNode<NodeQuery> cn) {
        NodeQuery nq = cn.getRoot();
        RelationQuery rq = nq.relationQuery();
        Relation baseRelation = rq.getRelation();
        List<Var> rootVars = baseRelation.getVars();
        // org.aksw.jenax.facete.treequery2.impl.FacetConstraints<ConstraintNode<NodeQuery>> constraints = rq.getFacetConstraints();

        MappedQuery result = null;

        // For each variable create the element for the constraints.
        // TODO How to handle cross-variable constraints cleanly?
        for (Var rootVar : rootVars) {


            TreeData<ScopedFacetPath> treeData = new TreeData<>();

            // FacetNodeImpl focusNode = (FacetNodeImpl)frq.getFacetedQuery().focus();
//            NodeQuery focusNode = nq;
//            TreeQueryNode tq = focusNode.node;
//
//            ScopedFacetPath focusPath = ScopedFacetPath.of(rootVar, tq.getFacetPath());

            // ScopedFacetPath thisPath = ConstraintNode.toScopedFacetPath(cn);
            // treeData.putItem(thisPath, ScopedFacetPath::getParent);

            ScopedFacetPath focusPath = nq.getRoot().getScopedFacetPath();

          // FacetPath focusPath = ElementGeneratorUtils.cleanPath(tq.getFacetPath());


            treeData.putItem(focusPath, ScopedFacetPath::getParent);
          // TreeDataUtils.putItem(treeData, focusPath, FacetPath::getParent);

            // org.aksw.jenax.facete.treequery2.impl.FacetConstraints<ConstraintNode<NodeQuery>> facetConstraints = rq.getFacetConstraints();
            Set<Expr> constraintExprs = ElementGeneratorLateral.createScopedConstraintExprs(rq);
            SetMultimap<ScopedFacetPath, Expr> constraintIndex = org.aksw.jenax.facete.treequery2.impl.FacetConstraints.createConstraintIndex(ScopedFacetPath.class, constraintExprs);


            // SetMultimap<ScopedFacetPath, Expr> constraintIndex = createConstraintIndex(constraints, treeData);

            UnaryRelation baseConcept = new Concept(baseRelation.getElement(), rootVar);

          // Generator<Var> varGen = GeneratorFromFunction.createInt().map(i -> Var.alloc("vv" + i));

          // Var rootVar = baseConcept.getVar();
//          Var superRootVar = Var.alloc("superRoot"); // Should not appear
          // DynamicInjectiveFunction<FacetPath, Var> ifn = DynamicInjectiveFunction.of(varGen);
//          FacetPath superRootPath = FacetPath.newAbsolutePath();
//          for (Var rootVar : baseConcept.getVar()) {
//              baseConcept.getVar();
//          }

          // ifn.getMap().put(FacetPath.newAbsolutePath(), rootVar);

          // FacetPathMapping fpm = ifn::apply;
          // Var rootVar = ifn.apply(PathOpsPPA.get().newRoot());

          //VarScope varScope = VarScope.of(rootVar);

            FacetPathMapping fpm = new FacetPathMappingImpl();
            ElementGenerator eltGen = new ElementGenerator(fpm, constraintIndex, focusPath);
            Traverser.forTree(treeData::getChildren).depthFirstPreOrder(treeData.getRootItems()).forEach(eltGen::addPath);

            return eltGen;
        }
        return null;
    }

    @Deprecated
    public static ElementGenerator configure(FacetedQueryImpl fq) {
        FacetedRelationQuery frq = fq.relationQuery;
//      UnaryRelation baseConcept;
//      FacetConstraints constraints;

        Relation baseRelation = frq.baseRelation.get();
        List<Var> rootVars = baseRelation.getVars();

        FacetConstraints constraints = frq.constraints;

        MappedQuery result = null;

        // For each variable create the element for the constraints.
        // TODO How to handle cross-variable constraints cleanly?
        for (Var rootVar : rootVars) {


            TreeData<ScopedFacetPath> treeData = new TreeData<>();

            FacetNodeImpl focusNode = (FacetNodeImpl)frq.getFacetedQuery().focus();
            TreeQueryNode tq = focusNode.node;

            ScopedFacetPath focusPath = ScopedFacetPath.of(rootVar, tq.getFacetPath());

          // FacetPath focusPath = ElementGeneratorUtils.cleanPath(tq.getFacetPath());


            treeData.putItem(focusPath, ScopedFacetPath::getParent);
          // TreeDataUtils.putItem(treeData, focusPath, FacetPath::getParent);


            SetMultimap<ScopedFacetPath, Expr> constraintIndex = createConstraintIndex(constraints, treeData);

            UnaryRelation baseConcept = new Concept(baseRelation.getElement(), rootVar);

          // Generator<Var> varGen = GeneratorFromFunction.createInt().map(i -> Var.alloc("vv" + i));

          // Var rootVar = baseConcept.getVar();
//          Var superRootVar = Var.alloc("superRoot"); // Should not appear
          // DynamicInjectiveFunction<FacetPath, Var> ifn = DynamicInjectiveFunction.of(varGen);
//          FacetPath superRootPath = FacetPath.newAbsolutePath();
//          for (Var rootVar : baseConcept.getVar()) {
//              baseConcept.getVar();
//          }

          // ifn.getMap().put(FacetPath.newAbsolutePath(), rootVar);

          // FacetPathMapping fpm = ifn::apply;
          // Var rootVar = ifn.apply(PathOpsPPA.get().newRoot());

          //VarScope varScope = VarScope.of(rootVar);

            FacetPathMapping fpm = new FacetPathMappingImpl();
            ElementGenerator eltGen = new ElementGenerator(fpm, constraintIndex, focusPath);
            Traverser.forTree(treeData::getChildren).depthFirstPreOrder(treeData.getRootItems()).forEach(eltGen::addPath);

            return eltGen;
        }
        return null;
    }


    public static SetMultimap<VarScope, Expr> createUnscopedConstraintExprs(Collection<Expr> scopedExprs) {
        SetMultimap<VarScope, Expr> result = HashMultimap.create();
        for (Expr expr : scopedExprs) {
            Set<ScopedFacetPath> sfps = NodeCustom.mentionedValues(ScopedFacetPath.class, expr);
            Multimap<VarScope, FacetPath> index = sfps.stream().collect(
                    Multimaps.toMultimap(ScopedFacetPath::getScope, ScopedFacetPath::getFacetPath, HashMultimap::create));

            if (index.keySet().isEmpty()) {
                System.err.println("WARN: Constraint without path reference found " + expr);
            }

            if (index.keySet().size() > 1) {
                throw new UnsupportedOperationException("Expressions with different scopes in facet paths currently not supported");
            }

            VarScope scope = index.keySet().iterator().next();
            Expr e = expr.applyNodeTransform(NodeCustom.mapValue(ScopedFacetPath.class, ScopedFacetPath::getFacetPath));
            result.put(scope, e);
        }
        return result;
    }

    public static SetMultimap<ScopedFacetPath, Expr> createConstraintIndex(FacetConstraints constraints,
            TreeData<ScopedFacetPath> treeData) {
        Collection<Expr> exprs = constraints.getExprs();
          SetMultimap<ScopedFacetPath, Expr> constraintIndex = HashMultimap.create();
          for (Expr expr : exprs) {
              // Set<FacetPath> paths = NodeFacetPath.mentionedPaths(expr);
              Set<ScopedFacetPath> paths = NodeCustom.mentionedValues(ScopedFacetPath.class, expr);

//              Map<FacetPath, FacetPath> remap = new HashMap<>();
//              for (FacetPath path : paths) {
//                  remap.put(path, ElementGeneratorUtils.cleanPath(path));
//              }

              // Maybe a mapping from TreeQueryNode to Var would be easier to handle?
//              Expr effectiveExpr = NodeTransformLib.transform(NodeTransformLib2.wrapWithNullAsIdentity(node -> {
//                  Node r = null;
//                  if (node instanceof NodeFacetPath) {
//                      FacetPath fp = ((NodeFacetPath) node).getValue().getFacetPath();
//                      FacetPath q = remap.get(fp);
//                      return q == null ? node : NodeFacetPath.of(() -> q);
//                  }
//                  return r;
//              }), expr);

              // We would now need a mapping from TreeQueryNode to the effective FacetPath!

              // for (FacetPath path : remap.values()) {
              for (ScopedFacetPath cleanPath : paths) {
                  // FacetPath cleanPath = ElementGeneratorUtils.cleanPath(path);

                  // Substitute the expression with the cleaned paths
                  // NodeFacetPath

                  treeData.putItem(cleanPath, ScopedFacetPath::getParent);
                  constraintIndex.put(cleanPath, expr);
              }
          }
        return constraintIndex;
    }


    public static MappedQuery createQuery(FacetedRelationQuery frq) {
//        UnaryRelation baseConcept;
//        FacetConstraints constraints;

        Relation baseRelation = frq.baseRelation.get();
        List<Var> rootVars = baseRelation.getVars();

        FacetConstraints constraints = frq.constraints;

        MappedQuery result = null;

        // For each variable create the element for the constraints.
        // TODO How to handle cross-variable constraints cleanly?
        for (Var rootVar : rootVars) {

            TreeData<FacetPath> treeData = new TreeData<>();

            FacetNodeImpl focusNode = (FacetNodeImpl)frq.getFacetedQuery().focus();
            TreeQueryNode tq = focusNode.node;
            FacetPath focusPath = tq.getFacetPath();
            treeData.putItem(focusPath, FacetPath::getParent);


            Collection<Expr> exprs = constraints.getExprs();
            // TODO Transform the set of exprs to ScopedFacetPaths

            SetMultimap<FacetPath, Expr> constraintIndex = HashMultimap.create();
            for (Expr expr : exprs) {
                Set<FacetPath> paths = NodeCustom.mentionedValues(FacetPath.class, expr);

                Map<FacetPath, FacetPath> remap = new HashMap<>();
                for (FacetPath path : paths) {
                    remap.put(path, ElementGeneratorUtils.cleanPath(path));
                }

                // Maybe a mapping from TreeQueryNode to Var would be easier to handle?
                Expr effectiveExpr = NodeTransformLib.transform(NodeTransformLib2.wrapWithNullAsIdentity(node -> {
                    Node r = null;
                    if (node instanceof NodeFacetPath) {
                        FacetPath fp = ((NodeFacetPath) node).getValue().getFacetPath();
                        FacetPath q = remap.get(fp);
                        return q == null ? node : NodeFacetPath.of(() -> q);
                    }
                    return r;
                }), expr);

                // We would now need a mapping from TreeQueryNode to the effective FacetPath!

                for (FacetPath path : remap.values()) {
                    FacetPath cleanPath = ElementGeneratorUtils.cleanPath(path);

                    // Substitute the expression with the cleaned paths
                    // NodeFacetPath

                    treeData.putItem(cleanPath, FacetPath::getParent);
                    constraintIndex.put(cleanPath, effectiveExpr);
                }
            }

            UnaryRelation baseConcept = new Concept(baseRelation.getElement(), rootVar);
            Predicate<FacetPath> isProjected = frq::isVisible;
            result = createQuery(baseConcept, treeData, constraintIndex, isProjected);
        }

        return result;
    }


    public Collection<Expr> transformAddScope(Collection<Expr> exprs, VarScope scope) {
        NodeTransform constraintTransform = NodeCustom.mapValue(ScopedFacetPath.class, (FacetPath fp) -> ScopedFacetPath.of(scope, fp));
        Collection<Expr> result = exprs.stream()
              .map(e -> e.applyNodeTransform(constraintTransform))
              .collect(Collectors.toList());
        return result;
    }

    /** Expressions based on NodeCustom<FacetPath> */
//    public SetMultimap<ScopedFacetPath, Expr> createConstraintIndex(Collection<Expr> rawExprs, VarScope scope) {
//        NodeTransform constraintTransform = NodeCustom.mapValue((FacetPath fp) -> ScopedFacetPath.of(scope, fp));
//        Collection<Expr> exprs = rawExprs.stream()
//        		.map(e -> e.applyNodeTransform(constraintTransform))
//        		.collect(Collectors.toList());
//
//        SetMultimap<ScopedFacetPath, Expr> result = org.aksw.jenax.treequery2.impl.FacetConstraints.createConstraintIndex(exprs);
//        return result;
//
//    }


    public static MappedQuery createQuery(UnaryRelation baseConcept, TreeData<FacetPath> rawTreeData, SetMultimap<FacetPath, Expr> constraintIndex, Predicate<FacetPath> isProjected) {

//        Generator<Var> varGen = GeneratorFromFunction.createInt().map(i -> Var.alloc("vv" + i));

        Var rootVar = baseConcept.getVar();
        VarScope scope = VarScope.of(rootVar);

        TreeData<ScopedFacetPath> treeData = rawTreeData.map(facetPath -> ScopedFacetPath.of(rootVar, facetPath));




        // rawTreeData.g


//        Var superRootVar = Var.alloc("superRoot"); // Should not appear
        // DynamicInjectiveFunction<FacetPath, Var> ifn = DynamicInjectiveFunction.of(varGen);
//        FacetPath superRootPath = FacetPath.newAbsolutePath();
//        for (Var rootVar : baseConcept.getVar()) {
//            baseConcept.getVar();
//        }


        // ElementGeneratorContext cxt = new ElementGeneratorContext(rootVar,  treeData, constraintIndex);


        // FacetPath focusPath = FacetPath.newAbsolutePath();
        // ifn.getMap().put(focusPath, rootVar);


        // FacetPathMapping fpm = ifn::apply;
        // Var rootVar = ifn.apply(PathOpsPPA.get().newRoot());


        // ElementGenerator eltGen = new ElementGenerator(fpm, constraintIndex, focusPath);


//        FacetPath testPath = FacetPath.newAbsolutePath(FacetStep.fwd(VOID.classPartition, null), FacetStep.fwd(VOID._class, null));
//        Expr testExprPath =  NodeFacetPath.asExpr(testPath);
//        Expr testConstraint = new E_Bound(testExprPath);
//
//        eltGen.analysePathModality(testConstraint);

        // Make sure to generate the elements for the mandatory paths

        // ElementGeneratorWorker worker = eltGen.createWorker();

        org.aksw.jenax.facete.treequery2.api.FacetPathMapping fpm = new FacetPathMappingImpl();
        PropertyResolver propertyResolver = new PropertyResolverImpl();
        ElementGeneratorWorker worker = new ElementGeneratorWorker(fpm, propertyResolver);

        // worker.getOrCreateContext(VarScope.of(rootVar)).setFacetTree(treeData);
        // worker.getOrCreateContext(

        // Traverser.<TreeData>forTree(TreeData::getChildren).;
        // Traverser.forTree(treeData::getChildren).depthFirstPreOrder(treeData.getRootItems()).forEach(cxt::addPath);
        ElementGeneratorContext cxt = worker.getOrCreateContext(scope);

        cxt
            .setFacetTree(rawTreeData)
            .setConstraintIndex(constraintIndex);


//        ElementGroup group = new ElementGroup();
//        // baseConcept.getElements().forEach(group::addElement);
//        for (FacetPath rootPath : treeData.getRootItems()) {
//            worker.accumulate(group, rootVar, rootPath, treeData::getChildren);
//            // ElementUtils.toElementList(elt).forEach(group::addElement);
//            // group.addElement(elt);
//        }
//        Element elt = group.size() == 1 ? group.get(0) : group;
        Element elt = worker.createElement().getElement();
        elt = ElementUtils.flatMerge(baseConcept.getElement(), elt);

        List<Var> visibleVars = cxt.getPathToVar().entrySet().stream()
                .filter(e -> isProjected.test(e.getKey()))
                .map(Entry::getValue)
                .collect(Collectors.toList());

        Query query = new Query();
        query.setQuerySelectType();
        query.setQueryPattern(elt);
        query.addProjectVars(visibleVars);

        BiMap<ScopedFacetPath, Var> varMap = worker.getPathToVar();

        System.err.println("Generated Query: " + query);
        // Map<FacetPathVar> map = cxt.getPathToVar();
        MappedQuery result = new MappedQuery(treeData, query, varMap.inverse());

        return result;
    }



//  public MappedElement createRelationForPath(FacetPath childPath, ) {
//      // TODO If the relation is of form ?s <p> ?o, then rewrite as ?s ?p ?o . FILTER(?p = <p>)
//
//      // FIXME This still breaks - because of conflict between the relation generated for the constraint and for the path
//      SetMultimap<FacetPath, Expr> effectiveConstraints = applySelfConstraints
//              ? constraintIndex
//              : ElementGeneratorUtils.hideConstraintsForPath(constraintIndex, childPath);
//
//      MappedElement result = new Worker(effectiveConstraints).createElement();
//      return result;
//  }


    public UnaryRelation getAvailableValuesAt(ScopedFacetPath path, boolean applySelfConstraints) {
        // FacetPath path = ElementGeneratorUtils.cleanPath(rawPath);

        SetMultimap<ScopedFacetPath, Expr> effectiveConstraints = applySelfConstraints
                ? constraintIndex
                : ElementGeneratorUtils.hideConstraintsForPath(constraintIndex, path);

        Var var = FacetPathMappingImpl.resolveVar(pathMapping, path).asVar();


        // Var var = Var.alloc("todoAddRoot");
        // ElementGeneratorContext cxt = new ElementGeneratorContext(var, facetTree, effectiveConstraints);
//        TreeData<ScopedFacetPath> facetTree = new TreeData<>();
//        if (focusPath != null) {
//            facetTree.putItem(focusPath, ScopedFacetPath::getParent);
//        }
        MappedElement me = new ElementGeneratorWorker(facetTree, effectiveConstraints, pathMapping, propertyResolver).createElement();

        // Var var = pathMapping.allocate(path);
        return new Concept(me.getElement(), var);
    }

    public TernaryRelation createRelationFacetValue(ScopedFacetPath focus, ScopedFacetPath facetPath, Direction direction, UnaryRelation pFilter, UnaryRelation oFilter, boolean applySelfConstraints, boolean includeAbsent) {
        Map<String, TernaryRelation> facetValues = createMapFacetsAndValues(facetPath, direction, false, applySelfConstraints, includeAbsent);
        // pFilter, oFilter,

        List<Element> elements = facetValues.values().stream()
                .map(e -> RelationUtils.rename(e, Arrays.asList(Vars.s, Vars.p, Vars.o)))
                .map(Relation::toTernaryRelation)
                .map(e -> pFilter == null ? e : e.joinOn(e.getP()).with(pFilter))
                .map(Relation::getElement)
                .collect(Collectors.toList());

        Element e = ElementUtils.unionIfNeeded(elements);

        TernaryRelation result = new TernaryRelationImpl(e, Vars.s, Vars.p, Vars.o);
        return result;
    }

    /**
     * Create a map for each constrained property to a relation that yields its values.
     *
     * The key of the map should probably be the facet FacetStep?!
     */
    public Map<String, TernaryRelation> createMapFacetsAndValues(ScopedFacetPath facetOriginPath, Direction direction, boolean applySelfConstraints, boolean negated, boolean includeAbsent) {
        // FacetPath facetOriginPath = ElementGeneratorUtils.cleanPath(rawFacetOriginPath);
        Map<String, TernaryRelation> result = new HashMap<>();

        // TODO We could reuse a TreeData structure to avoid iterating all paths of all constraints?
        Set<ScopedFacetPath> constrainedPaths = constraintIndex.keySet();
        Set<ScopedFacetPath> constrainedChildPaths = FacetPathUtils.getDirectChildren(facetOriginPath, direction, constrainedPaths);

        // ElementGenerator.createQuery(parent.facetedQuery.relationQuery, x -> true);
        // this.pathMapping.allocate(facetOriginPath)

        Var focusVar = FacetPathMappingImpl.resolveVar(pathMapping, focusPath).asVar();

        for(ScopedFacetPath childPath : constrainedChildPaths) {
            FacetStep facetStep = facetOriginPath.relativize(childPath).toSegment();
            // FacetStep facetStep = facetOriginPath.getFacetPath().relativize(childPath.getFacetPath()).toSegment();

            MappedElement mr = createRelationForPath(childPath, applySelfConstraints, negated, includeAbsent);
            Var childVar = mr.getVar(childPath);

            // Note sure if this the best place where to add the baseElement
            List<Element> elts = ElementUtils.flatMergeList(baseElement, mr.getElement());
            elts.add(new ElementBind(Vars.p, NodeValue.makeNode(facetStep.getNode())));

            TernaryRelation br = new TernaryRelationImpl(ElementUtils.groupIfNeeded(elts), focusVar, Vars.p, childVar);
            String pStr = childPath.getParent() == null ? "" : childPath.getFacetPath().getFileName().toSegment().getNode().getURI();

            // Substitute the empty predicate by the empty string
            // The empty string predicate (zero length path) is different from
            // the set of remaining predicates indicated by a null entry in the result map
            pStr = pStr == null ? "" : pStr;


            // Skip adding the relation empty string if the relation is empty
            // if(!(pStr.isEmpty() && br.isEmpty())) {
              result.put(pStr, br);
            // }
        }

      // Add the predicate/value paths and generate the element again

      // exclude all predicates that are constrained
      // FIXME Was getRemainingFacets
        BinaryRelation brrx = getRemainingFacetsWithoutAbsent(facetOriginPath, direction, negated, includeAbsent);
        TernaryRelation brr = new TernaryRelationImpl(brrx.getElement(), focusVar, brrx.getSourceVar(), brrx.getTargetVar());

      // Build the constraint to remove all prior properties
        ExprList constrainedPredicates = new ExprList(result.keySet().stream()
            .filter(pStr -> !pStr.isEmpty())
            .map(NodeFactory::createURI)
            .map(NodeValue::makeNode)
            .collect(Collectors.toList()));

        if(!constrainedPredicates.isEmpty()) {
            List<Element> foo = brr.getElements();
            foo.add(new ElementFilter(new E_NotOneOf(new ExprVar(brrx.getSourceVar()), constrainedPredicates)));
            brr = new TernaryRelationImpl(ElementUtils.groupIfNeeded(foo), brr.getS(), brr.getP(), brr.getO());
        }

        result.put(null, brr);

        return result;
    }

    /**
     */
    public MappedElement createRelationForPath(ScopedFacetPath childPath, boolean applySelfConstraints, boolean negated, boolean includeAbsent) {
        // TODO If the relation is of form ?s <p> ?o, then rewrite as ?s ?p ?o . FILTER(?p = <p>)

        // FIXME This still breaks - because of conflict between the relation generated for the constraint and for the path
        SetMultimap<ScopedFacetPath, Expr> effectiveConstraints = applySelfConstraints
                ? constraintIndex
                : ElementGeneratorUtils.hideConstraintsForPath(constraintIndex, childPath);

        MappedElement result = new ElementGeneratorWorker(facetTree, effectiveConstraints, pathMapping, propertyResolver).createElement();
        return result;
    }


    public BinaryRelation getRemainingFacetsWithoutAbsent(ScopedFacetPath sourceFacetPath, Direction direction, boolean negated, boolean includeAbsent) {
        FacetStep pStep = FacetStep.of(NodeUtils.ANY_IRI, direction, null, FacetStep.PREDICATE);
        FacetStep oStep = FacetStep.of(NodeUtils.ANY_IRI, direction, null, FacetStep.TARGET);
        ScopedFacetPath pPath = sourceFacetPath.resolve(pStep);
        ScopedFacetPath oPath = sourceFacetPath.resolve(oStep);

        TreeData<ScopedFacetPath> newTree = facetTree.cloneTree();
        newTree.putItem(pPath, ScopedFacetPath::getParent);
        newTree.putItem(oPath, ScopedFacetPath::getParent);

        ElementGeneratorWorker worker = new ElementGeneratorWorker(newTree, constraintIndex, pathMapping, propertyResolver);
        worker.declareMandatoryPath(oPath);

        MappedElement me = worker.createElement();
        Element elt = ElementUtils.flatMerge(baseElement, me.getElement());

//        List<Element> foo = new ArrayList<>();
//        if (baseElement != null) {
//            foo.addAll(ElementUtils.toElementList(baseElement));
//        }
//        foo.addAll(brr.getElements());


        Var pVar = me.getVar(pPath);
        Var oVar = me.getVar(oPath);
        BinaryRelation result = new BinaryRelationImpl(elt, pVar, oVar);
        return result;
    }

    /* Methods below are copied from Facete3's FacetedQueryGenerator and should probably be migrated to this class! */


//    // UnaryRelation baseConcept, P focusPath, P facetPath,
//    public Map<String, TernaryRelation> getFacetValuesCore(FacetPath facetPath, UnaryRelation pFilter, UnaryRelation oFilter, Direction direction, boolean negated, boolean applySelfConstraints, boolean includeAbsent) {
//
//        // Get the focus element
//        // BinaryRelation focusRelation = mapper.getOverallRelation(focusPath);
//        // TODO We may want to use variables of focusRelation as input to blacklisting in var allocation
//
//        Var focusVar = pathMapping.allocate(focusPath);
//
//        //boolean applySelfConstraints = false;
//        Map<String, BinaryRelation> facets = createMapFacetsAndValues(facetPath, direction, applySelfConstraints, negated, includeAbsent);
//
//        Map<String, TernaryRelation> result = new HashMap<>();
//        for(Entry<String, BinaryRelation> facet : facets.entrySet()) {
//        	BinaryRelation br = facet.get
//        	result.put(facet.getKey(), new TernaryRelationImpl(facet.getValue().getElement(), focusVar, focusVar, focusVar));
//
////
////            // TODO Factor out this block into a common method
////            Element e5;
////            {
////                UnaryRelation bc = Optional.ofNullable(baseConcept)
////                        .orElse(ConceptUtils.createSubjectConcept());
////
////                //Var resultVar = (Var)mapper.getNode(facetPath);
////
////                P rootPath = TreeUtils.getRoot(facetPath, pathAccessor::getParent);
////                Var rootVar = (Var)mapper.getNode(rootPath);
////
////                UnaryRelation c4 = new Concept(e4, rootVar);
////
////
////                Relation tmp = c4.prependOn(rootVar).with(bc);
////                e5 = tmp.getElement();
////            }
//
//
////             TernaryRelation tr = new TernaryRelationImpl(e5, focusRelation.getTargetVar(), rel.getSourceVar(), rel.getTargetVar());
//
////			tr = new TernaryRelationImpl(ElementUtils.createElementGroup(ImmutableList.<Element>builder()
////				.addAll(tr.getElements())
////				.add(new ElementFilter(new E_LogicalOr(
////						new E_LogicalNot(new E_Bound(new ExprVar(tr.getP()))),
////						new E_LogicalNot(new E_IsBlank(new ExprVar(tr.getP()))))))
////				.build()),
////				tr.getS(),
////				tr.getP(),
////				tr.getO());
//
//
////            String p = facet.getKey();
////            result.put(p, tr);
//        }
//
//        return result;
//    }
//
//    public BinaryRelation getRemainingFacets(P focusPath, P facetOriginPath, boolean isReverse, SetMultimap<P, Expr> constraintIndex, boolean negated, boolean includeAbsent) {
//        BinaryRelation result = includeAbsent
//                ? getRemainingFacetsWithAbsent(focusPath, facetOriginPath, isReverse, constraintIndex, negated)
//                : getRemainingFacetsWithoutAbsent(facetOriginPath, isReverse, constraintIndex, negated, includeAbsent);
//
//        return result;
//    }
//
//
//    public BinaryRelation getRemainingFacetsWithAbsent(P focusPath, P facetOriginPath, boolean isReverse, SetMultimap<P, Expr> constraintIndex, boolean negated) {
//        boolean includeAbsent = true;
//
//        Element tripleEl = ElementUtils.createElement(QueryFragment.createTriple(isReverse, Vars.s, Vars.p, Vars.o));
//        Element baseEl = new ElementOptional(tripleEl);
//
//        BinaryRelation br = new BinaryRelationImpl(baseEl, Vars.s, Vars.o);
//
//
//        // TODO Combine rel with the constraints
//        BinaryRelation rel = mapper.getOverallRelation(facetOriginPath);
//
//        BinaryRelation tmp = createConstraintRelationForPath(facetOriginPath, null, br, Vars.p, constraintIndex, false, includeAbsent);
//
//
//        List<Element> elts = new ArrayList<>();
//
//        // In this case we need to inject the set of facets:
//        // so that we can left join the focus resources
//
//        // We are only interested in the null entry here which denotes the set of
//        // unconstraint facets
//        // Te constraint facets are properly processed individually
//        //Map<String, BinaryRelation> rawRelations = createMapFacetsAndValues(facetOriginPath, isReverse, false, false, false);
//
//        Map<String, TernaryRelation> rawRelations3 = getFacetValuesCore(baseConcept, focusPath, facetOriginPath, null, null, isReverse, negated, false, false);
//
//        TernaryRelation tr = rawRelations3.get(null);
//        UnaryRelation rawFacetConcept = tr.project(tr.getP()).toUnaryRelation();
//
////		Map<String, TernaryRelation> rawRelation = Collections.singletonMap(null, rawRelations.get(null).));
//        //Map<String, TernaryRelation> relations = Collections.singletonMap(null, rawBr);
//
//        //UnaryRelation rawFacetConcept = createConceptFacets(relations, null);
//
//        //UnaryRelation rawFacetConcept = createConceptFacets(facetOriginPath, isReverse, false, null);
//
//        // This should make all variables of the facet concept
//        // - except for ?p - distinct from the tmp
//        UnaryRelation facetConcept = rawFacetConcept.rename(varName -> "opt_" + varName, Vars.p).toUnaryRelation();
//
//        //UnaryRelation facetConcept = rawFacetConcept.joinOn(Vars.p).yieldRenamedFilter(rawFacetConcept).toUnaryRelation();
//
//        elts.addAll(facetConcept.getElements());
//
//
//        elts.addAll(rel.getElements());
//        elts.addAll(tmp.getElements());
//
//        BinaryRelation result = new BinaryRelationImpl(
//                ElementUtils.groupIfNeeded(elts), tmp.getSourceVar(), tmp.getTargetVar()
//        );
//
//        return result;
//    }
//
//    public BinaryRelation getRemainingFacetsWithoutAbsent(FacetPath sourceFacetPath, Direction direction, boolean negated, boolean includeAbsent) {
//
//        Element baseEl = ElementUtils.createElementTriple(Vars.s, Vars.p, Vars.o, direction.isForward());
//
//        BinaryRelation br = new BinaryRelationImpl(baseEl, Vars.s, Vars.o);
//
//        // TODO Combine rel with the constraints
//        BinaryRelation rel = mapper.getOverallRelation(sourceFacetPath);
//        BinaryRelation tmp = createConstraintRelationForPath(sourceFacetPath, null, br, Vars.p, constraintIndex, false, includeAbsent);
//
//        List<Element> elts = new ArrayList<>();
//
//        elts.addAll(rel.getElements());
//        elts.addAll(tmp.getElements());
//
//        BinaryRelation result = new BinaryRelationImpl(
//                ElementUtils.groupIfNeeded(elts), tmp.getSourceVar(), tmp.getTargetVar()
//        );
//
//        return result;
//    }
//
//    /**
//     * Returns a binary relation with facet - facet value columns.
//     *
//     * Attemps to rename variables of the facetRelation as to not conflict
//     * with the variables of paths.
//     *
//     *
//     * Issue: facetRelation is not relative to basePath but to root,
//     * so the connection variable is that of root
//     *
//     * @param rootPath only used to obtain the connecting variable
//     * @param facetRelation
//     * @param pVar
//     * @param effectiveConstraints
//     * @param negate Negate the constraints - this yields all facet+values unaffected by the effectiveConstraints do NOT apply
//     * @return
//     */
//    public BinaryRelation createConstraintRelationForPath(FacetPath rootPath, FacetPath childPath, BinaryRelation facetRelation, Var pVar, Multimap<FacetPath, Expr> constraintIndex, boolean negate, boolean includeAbsent) {
//
////		Collection<Element> elts = createElementsFromConstraintIndex(constraintIndex,
////				p -> !negate ? false : (childPath == null ? true : Objects.equals(p, childPath)));
//
//        Collection<Element> elts = createElementsFromConstraintIndex(constraintIndex,
//                //p -> Objects.equals(childPath, p),
//                p -> !negate ? false : (childPath == null ? true : Objects.equals(p, childPath)));
//
//
//        //Collection<Element> elts = createElementsForExprs(effectiveConstraints, negate);
//        //BinaryRelation tmp = createRelationForPath(facetRelation, effectiveConstraints, negate);
//        //List<Element> elts = tmp.getElements();
//
//
//
//        Var s = (Var)mapper.getNode(rootPath);//.asVar();
//
//        // Rename all instances of ?p and ?o
//
//        Set<Var> forbiddenVars = new HashSet<>();
//        for(Element e : elts) {
//            Collection<Var> v = PatternVars.vars(e);
//            forbiddenVars.addAll(v);
//        }
//
//        //forbiddenVars.addAll(facetRelation.getVarsMentioned());
//
//        // Set up the relation for the facets:
//        // Make sure that none of its ?p and ?o variables collides with variables
//        // of the constraint elements
//        // Rename all instances of 'p' and 'o' variables
//        // Also make sure that vars of facetRelation are not remapped among themselves
//        Set<Var> vars = facetRelation.getVarsMentioned();//new HashSet<>(Arrays.asList(Vars.p, Vars.o));
////		vars.remove(facetRelation.getSourceVar());
////		vars.remove(facetRelation.getTargetVar());
//
//        //forbiddenVars.addAll(vars);
//
//        Map<Var, Var> rename = VarUtils.createDistinctVarMap(forbiddenVars, Arrays.asList(pVar, facetRelation.getTargetVar()), true, null);//VarGeneratorBlacklist.create(forbiddenVars));
//        rename.put(facetRelation.getSourceVar(), s);
////		rename.put(s, facetRelation.getSourceVar());
//
//        // Connect the source of the facet relation to the variable of the
//        // base path
//        //Map<Var, Var> r2 = new HashMap<>();
//        //rename.put(facetRelation.getSourceVar(), s);
//
//        BinaryRelation renamedFacetRelation = facetRelation.applyNodeTransform(NodeTransformRenameMap.create(rename));
//
//        //s = rename.getOrDefault(s, s);
//
////		List<Element> es = new ArrayList<>();
////		for(Element e : elts) {
////			Element x = ElementUtils.createRenamedElement(e, rename);
////			es.add(x);
////		}
//
//        //boolean isReverse = pathAccessor.isReverse(path);
//        //Triple t = QueryFragment.createTriple(isReverse, s, Vars.p, Vars.o);
//        //es.add(facetRelation.getElement());//ElementUtils.createElement(t));
//
//        elts.addAll(renamedFacetRelation.getElements());
//
//        //BinaryRelation result = new BinaryRelation(ElementUtils.groupIfNeeded(es), Vars.p, Vars.o);
//        BinaryRelation result = new BinaryRelationImpl(ElementUtils.groupIfNeeded(elts), pVar, rename.getOrDefault(facetRelation.getTargetVar(), facetRelation.getTargetVar()));
//
//        return result;
//    }
//
//
//    public <P> Map<P, BinaryRelation> allocatePathRelations(PathToRelationMapper<P> mapper, Multimap<FacetPath, Expr> constraintIndex) {
//        Map<P, BinaryRelation> result = new LinkedHashMap<>();
//
//        for(Entry<FacetPath, Collection<Expr>> e : constraintIndex.asMap().entrySet()) {
////			llCollection<P> paths = PathAccessorImpl.getPathsMentioned(expr, pathAccessor::tryMapToPath).values();
////
////			// FIXME And another issue:
////			// Element creation for absent-constrainted paths has be done on the path level
////			// Presently we operate expr-centric: whenever an expr references a Path we
////			// create the path's element.
//
//            FacetPath path = e.getKey();
//            Collection<Expr> exprs = e.getValue();
//            // Deal with absent values
//            boolean containsAbsent = FacetedQueryGenerator.containsAbsent(exprs);
//            BinaryRelation br = createRelationForPath(mapper, path, containsAbsent);
//
//            result.put(path, br);
//        }
//
//        return result;
//    }
//

    /** Configure and execute the worker to create a graph pattern for the given path*/
//    public static BinaryRelation createRelationForPath(FacetPath childPath, boolean includeAbsent) {
//        BinaryRelation result;
//        if(includeAbsent) {
//
//            FacetPath parent = childPath.getParent();
//
//            // Somewhat hacky: First create the overall path in order to
//            // allocate variables in the mapper
//            BinaryRelation tmp = mapper.getOverallRelation(childPath);
//
//            // But actually we only need the path to the parent first
//            BinaryRelation br = mapper.getOverallRelation(parent);
//
//            // We need to adjust the variable naming of the last step
//            // according to the mapper's state, so rename the variables
//            BinaryRelation rawLastStep = pathAccessor.getReachingRelation(childPath);
//
//
//            // TODO Wrap this renaming construct up in the API
//            BinaryRelation helper = new BinaryRelationImpl(new ElementGroup(), br.getTargetVar(), tmp.getTargetVar());
//            BinaryRelation lastStep = helper.joinOn(helper.getSourceVar(), helper.getTargetVar())
//                    .with(rawLastStep)
//                    .toBinaryRelation();
//
//
//            Collection<Element> elts = new ArrayList<>();
//            elts.addAll(br.getElements());
//            elts.add(new ElementOptional(lastStep.getElement()));
//            //elts.add(new ElementFilter(new E_LogicalNot(new E_Bound(new ExprVar(lastStep.getTargetVar())))));
//
//            Element group = ElementUtils.groupIfNeeded(elts);
//
//            result = new BinaryRelationImpl(group, tmp.getSourceVar(), lastStep.getTargetVar());
//
//        } else {
//            result = mapper.getOverallRelation(childPath);
//        }
//        return result;
//    }


//
//    // Create a query for the facets at the given path
//    public void facets(FacetPath facetPath, Direction direction, boolean includeAbsent) {
//
//        Map<String, TernaryRelation> relations = getFacetValuesCore(null, null, direction, false, false, includeAbsent);
//
//        UnaryRelation concept = FacetedQueryGenerator.createConceptFacets(relations, null);
//
////        FacetedDataQuery<RDFNode> result = new FacetedDataQueryImpl<>(
////                null, // connection
////                concept.getElement(),
////                concept.getVar(),
////                null,
////                RDFNode.class);
//    }
//
//
//    /**
//     * Map each predicate reachable from the sourceFacetPath to a graph pattern
//     */
//    public Map<String, BinaryRelation> createMapFacetsAndValues(FacetPath facetOriginPath, boolean isForward, boolean applySelfConstraints, boolean negated, boolean includeAbsent) {
//
//        // SetMultimap<P, Expr> constraintIndex = indexConstraints(pathAccessor, constraints);
//
//        Map<String, BinaryRelation> result = new HashMap<>();
//
//        Set<FacetPath> mentionedPaths = constraintIndex.keySet();
//        Set<FacetPath> constrainedChildPaths = FacetPathUtils.getDirectChildren(facetOriginPath, isForward, mentionedPaths);
//
//        for(FacetPath childPath : constrainedChildPaths) {
//            BinaryRelation br = createRelationForPath(childPath, constraintIndex, applySelfConstraints, negated, includeAbsent);
//
//            String pStr = childPath.getParent() == null ? "" : childPath.getFileName().toSegment().getNode().getURI();
//
//            // Substitute the empty predicate by the empty string
//            // The empty string predicate (zero length path) is different from
//            // the set of remaining predicates indicated by a null entry in the result map
//            pStr = pStr == null ? "" : pStr;
//
//
//            // Skip adding the relation empty string if the relation is empty
//            if(!(pStr.isEmpty() && br.isEmpty())) {
//                result.put(pStr, br);
//            }
//        }
//
//        // exclude all predicates that are constrained
//
//        BinaryRelation brr = getRemainingFacets(focusPath, facetOriginPath, isForward, constraintIndex, negated, includeAbsent);
//
//        // Build the constraint to remove all prior properties
//        ExprList constrainedPredicates = new ExprList(result.keySet().stream()
//                .filter(pStr -> !pStr.isEmpty())
//                .map(NodeFactory::createURI)
//                .map(NodeValue::makeNode)
//                .collect(Collectors.toList()));
//
//        if(!constrainedPredicates.isEmpty()) {
//            List<Element> foo = brr.getElements();
//            foo.add(new ElementFilter(new E_NotOneOf(new ExprVar(brr.getSourceVar()), constrainedPredicates)));
//            brr = new BinaryRelationImpl(ElementUtils.groupIfNeeded(foo), brr.getSourceVar(), brr.getTargetVar());
//        }
//
//        result.put(null, brr);
//
//        return result;
//    }
//


}



//public ElementGeneratorWorker createWorkerForPath(FacetPath facetPath) {
//  SetMultimap<FacetPath, Expr> localIndex = ElementGeneratorUtils.hideConstraintsForPath(constraintIndex, facetPath);
//  return new ElementGeneratorWorker(facetTree, localIndex, pathMapping, propertyResolver);
//}
//
//public ElementGeneratorWorker createWorker() {
//  return new ElementGeneratorWorker(facetTree, constraintIndex, pathMapping, propertyResolver);
//}



// protected Element baseElement; // This element also exists in eltPathToAcc.get(FacetPath.newAbsolutePath())
// protected Map<VarScope, TreeData<FacetPath>> scopeToFacetTree = new HashMap<>(); //new TreeData<>();
// protected TreeData<ScopedFacetPath> facetTree;
// protected Map<VarScope, TreeData<FacetPath>> facetTree = new HashMap<>();
// protected Map<VarScope, ElementGenerator.Context> scopeToContext = new HashMap<>();

//static class Context {
//    /** Mapping of element paths (FacetPaths with the component set to the TUPLE constant) */
//    protected Map<FacetPath, ElementAcc> eltPathToAcc = new LinkedHashMap<>();
//
//}
