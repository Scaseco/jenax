package org.aksw.facete.v4.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.facete.v3.api.TreeData;
import org.aksw.facete.v3.api.TreeDataMap;
import org.aksw.facete.v3.api.VarScope;
import org.aksw.jena_sparql_api.data_query.impl.FacetedQueryGenerator;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.jenax.arq.util.node.NodeCustom;
import org.aksw.jenax.arq.util.node.NodeTransformLib2;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.facete.treequery2.api.FacetPathMapping;
import org.aksw.jenax.facete.treequery2.api.ScopedFacetPath;
import org.aksw.jenax.facete.treequery2.impl.FacetConstraints;
import org.aksw.jenax.facete.treequery2.impl.FacetPathMappingImpl;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformExpr;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Create individual elements for a facet paths.
 * The class {@link ElementGenerator} can generate "high level" queries and uses this class as a worker to carry out
 * specific sub tasks.
 */
public class ElementGeneratorWorker {
    // protected Map<VarScope, TreeData<FacetPath>> facetTree;
    protected Map<VarScope, ElementGeneratorContext> scopeToContext = new LinkedHashMap<>();
    protected FacetPathMapping pathMapping;
    protected PropertyResolver propertyResolver;

    public ElementGeneratorWorker(FacetPathMapping pathMapping, PropertyResolver propertyResolver) {
        this(new TreeData<>(), HashMultimap.create(), pathMapping, propertyResolver);
    }

    public ElementGeneratorWorker(TreeData<ScopedFacetPath> facetTree, SetMultimap<ScopedFacetPath, Expr> constraintIndex, FacetPathMapping pathMapping, PropertyResolver propertyResolver) {
        this.pathMapping = pathMapping;
        this.propertyResolver = propertyResolver;
        // this.constraintIndex = constraintIndex;
        setFacetTree(facetTree);
        setConstraintIndex(constraintIndex);
    }

    public void setConstraintIndex(SetMultimap<ScopedFacetPath, Expr> constraintIndex) {
        SetMultimap<VarScope, Expr> localIndices = ElementGenerator.createUnscopedConstraintExprs(constraintIndex.values());

        for (Entry<VarScope, Collection<Expr>> e : localIndices.asMap().entrySet()) {
            ElementGeneratorContext cxt = getOrCreateContext(e.getKey());
            SetMultimap<FacetPath, Expr> localConstraintIndex = FacetConstraints.createConstraintIndex(FacetPath.class, e.getValue());
            cxt.setConstraintIndex(localConstraintIndex);
        }

        for (Expr expr : new ArrayList<>(constraintIndex.values())) {
            analysePathModality(expr);
        }
    }

    public void setFacetTree(TreeData<ScopedFacetPath> facetTree) {
        for (ScopedFacetPath rootPath : facetTree.getRootItems()) {
            VarScope scope = rootPath.getScope();
            ElementGeneratorContext cxt = getOrCreateContext(scope);
            TreeData<FacetPath> tree = facetTree.map(ScopedFacetPath::getFacetPath);
            cxt.setFacetTree(tree);
        }
    }

    public FacetPathMapping getPathMapping() {
        return pathMapping;
    }

    public PropertyResolver getPropertyResolver() {
        return propertyResolver;
    }

    public BiMap<ScopedFacetPath, Var> getPathToVar() {
        BiMap<ScopedFacetPath, Var> result = scopeToContext.values().stream().flatMap(cxt -> cxt.getPathToVar().entrySet().stream()
                .map(e -> new SimpleEntry<>(ScopedFacetPath.of(cxt.scope, e.getKey()), e.getValue())))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (u, v) -> u, HashBiMap::create));
        return result;
    }

    /**
     * Checks for whether this expression references any paths that need to be mandatory.
     * Elements created for the segments along such a path will not be wrapped in OPTIONAL blocks.
     */
    public void analysePathModality(Expr expr) {
        //public void addExpr(Expr expr) {
        Set<ScopedFacetPath> paths = NodeCustom.mentionedValues(ScopedFacetPath.class, expr);

        boolean isMandatory = !FacetedQueryGenerator.isAbsent(expr);

        for (ScopedFacetPath path : paths) {
            // addPath(path);
            // constraintIndex.put(path, expr);

            if (isMandatory) {
                declareMandatoryPath(path);
            }
        }
    }

    /** Mark a path as mandatory. This makes all parents also mandatory. */
    public void declareMandatoryPath(ScopedFacetPath scopedPath) {
        ElementGeneratorContext cxt = getOrCreateContext(scopedPath.getScope());
        declareMandatoryPath(cxt, scopedPath.getFacetPath());
    }

    public ElementGeneratorContext getOrCreateContext(VarScope scope) {
        ElementGeneratorContext result = scopeToContext.computeIfAbsent(scope, sc -> {
            return new ElementGeneratorContext(sc);
        });
        return result;
    }

    public void declareMandatoryPath(ElementGeneratorContext cxt, FacetPath path) {
        FacetPath current = path;
        Set<FacetPath> mandatoryElementIds = cxt.mandatoryElementIds;
        while (current != null) {
            FacetPath eltId = FacetPathUtils.toElementId(current);
            if (!mandatoryElementIds.contains(eltId)) {
                mandatoryElementIds.add(eltId);
                current = current.getParent();
            } else {
                break;
            }
        }
    }

    /**
     * Create the element for the last facet step of a facet path (without recursion)
     */
    public ElementAcc allocateEltAcc(Var parentVar, Var targetVar, ScopedFacetPath scopedPath) {
        ElementGeneratorContext cxt = getOrCreateContext(scopedPath.getScope());
        return allocateEltAcc(cxt, parentVar, targetVar, scopedPath.getFacetPath());
    }

    public ElementAcc allocateEltAcc(ElementGeneratorContext cxt, Var parentVar, Var targetVar, FacetPath path) {
        // FIXME Naively adding optional elements does not work when facet paths are mapped to BIND elements
        // In the example below, ?bar will be unbound:
        // BIND("foo" AS ?foo) OPTIONAL { BIND(?foo AS ?bar) }

        FacetPath eid = FacetPathUtils.toElementId(path);
        // ElementGroup container = new ElementGroup();
        boolean isMandatory = cxt.mandatoryElementIds.contains(eid);

        Element coreElt = null;
        Node secondaryNode;
        if (!path.getSegments().isEmpty()) {
            coreElt = createElementForLastStep(cxt, parentVar, targetVar, path);

            //container.addElement(coreElt);
        } else {
            coreElt = new ElementGroup();
            // Add the base element if this is the root path
            // container.addElement(baseElement);
        }

        boolean isEmptyGroup = coreElt instanceof ElementGroup && ((ElementGroup)coreElt).isEmpty();

        // Element root = isMandatory || coreElt instanceof ElementBind ? container : new ElementOptional(container);
        BiFunction<Element, List<Element>, Element> combiner =  isMandatory || coreElt instanceof ElementBind || isEmptyGroup
                ? ElementAcc::collectIntoGroup
                : ElementAcc::collectIntoOptionalGroup;


        return new ElementAcc(coreElt, combiner);
    }

    public Element createElementForLastStep(ElementGeneratorContext cxt, Var parentVar, Var targetVar, FacetPath path) {
        Element coreElt;
        FacetStep step = path.getFileName().toSegment();

        Node secondaryNode;
        Node predicateNode = step.getNode();
        boolean isFwd = step.isForward();

        Node c = step.getTargetComponent();

        if (NodeUtils.ANY_IRI.equals(predicateNode)) {
            Node toggledComponent = FacetStep.isTarget(c) ? FacetStep.PREDICATE : FacetStep.TARGET;
            FacetStep s = step.copyStep(toggledComponent);
        // FacetStep toggledTarget = step.toggleTarget();
            FacetPath predicatePath = path.resolveSibling(s);
            //secondaryNode = FacetPathMappingImpl.resolveVar(pathMapping, cxt.scope.getScopeName(), targetVar, path).asVar(); // pathMapping.allocate(path.resolveSibling(s));
            secondaryNode = FacetPathMappingImpl.resolveVar(pathMapping, cxt.scope.getScopeName(), cxt.scope.getStartVar(), predicatePath).asVar();
            // secondaryNode = path.transformPath(fp -> fp.resolveSibling(s)).toScopedVar(pathMapping).asVar();
        } else {
            secondaryNode = predicateNode;
        }

        // Relation rename:
        // target(s) have given names
        // source(s) have given names
        // all further variables in the scope of the source(s)

        if (FacetStep.isTarget(c)) {
            // coreElt = propertyResolver.resolve(parentVar, secondaryNode, targetVar, isFwd);
            Relation rel = propertyResolver.resolve(secondaryNode);

            // If the facet step is inverted then swap the first and last variables of the relation
            if (step.getDirection().isBackward()) {
                List<Var> swappedVars = new ArrayList<>(rel.getVars());
                Var first = swappedVars.get(0);
                int lastIndex = swappedVars.size() - 1;
                Var last = swappedVars.get(lastIndex);
                swappedVars.set(0,  last);
                swappedVars.set(lastIndex, first);
                rel = rel.project(swappedVars);
            }


            // FIXME Adapt the relation w.r.t parentVar, targetVar and direction
            // RelationUtils.rename(
            String scopeName = cxt.getScope().getScopeName();
            Map<Var, Var> varRename = new HashMap<>();
            List<Var> vars = rel.getVars();
            int n = vars.size();

            int start = 1;
            int end = n - 1;
            List<Var> intermediateVars = start >= end ? Collections.emptyList() : vars.subList(start, end);

            varRename.put(vars.get(n - 1), targetVar);
            varRename.put(vars.get(0), parentVar);

            for (Var v : intermediateVars) {
                Var scopedVar = FacetPathMappingImpl.resolveVar(pathMapping, scopeName, targetVar, path).asVar();
                varRename.put(v, scopedVar);
            }
            Relation finalRel = rel.applyNodeTransform(NodeTransformLib2.wrapWithNullAsIdentity(varRename::get));
            coreElt = finalRel.getElement();
            // coreElt = ElementUtils.createElementTriple(parentVar, secondaryNode, targetVar, isFwd);
        } else {
            coreElt = ElementUtils.createElementTriple(parentVar, targetVar, secondaryNode, isFwd);
        }
        return coreElt;
    }

    public void allocateElements(Expr expr) {
        Collection<ScopedFacetPath> paths = NodeCustom.mentionedValues(ScopedFacetPath.class, expr);
        for(ScopedFacetPath path : paths) {
            allocateElement(path);
        }
    }

    public Var allocateElement(ScopedFacetPath path) {
        ElementGeneratorContext cxt = getOrCreateContext(path.getScope());
        return allocateElement(cxt, path.getFacetPath());
    }

    public Var allocateElement(ElementGeneratorContext cxt, FacetPath path) {
        FacetPath parentPath = path.getParent();
        FacetPath eltId = FacetPathUtils.toElementId(path);

        ElementAcc elementAcc = cxt.facetPathToAcc.get(eltId);
        Var targetVar;
        if (elementAcc == null) {
            Var parentVar;
            // targetVar = pathToVar.computeIfAbsent(path, pathMapping::allocate);
            targetVar = FacetPathMappingImpl.resolveVar(pathMapping, cxt.scope, path).asVar(); // path.toScopedVar(pathMapping).asVar();

            if (parentPath != null) {
                parentVar = allocateElement(cxt, parentPath);
            } else {
                parentVar = targetVar;
            }

            elementAcc = allocateEltAcc(cxt, parentVar, targetVar, path);
            cxt.facetPathToAcc.addItem(eltId.getParent(), eltId);
            cxt.facetPathToAcc.put(eltId, elementAcc);

            // Create the ElementAcc for the path if it hasn't happened yet
//                Iterable<FacetPath> children = getChildren.apply(path);
//                if (children != null && children.iterator().hasNext()) {
//                    for (FacetPath subPath : children) {
//                        // If there is no accumulator for the child then visit it
//                        accumulate(elementAcc.getContainer(), targetVar, subPath, getChildren);
//                    }
//                }
//
//                if (!elementAcc.getContainer().isEmpty()) {
//                    ElementUtils.copyElements(parentAcc, elementAcc.getResultElement());
//                }
        } else {
            // targetVar = cxt.pathToVar.computeIfAbsent(path, pathMapping::allocate);
            targetVar = cxt.pathToVar.computeIfAbsent(path, p ->
                FacetPathMappingImpl.resolveVar(pathMapping, cxt.scope, p).asVar());
        }

        return targetVar;
    }

    /**
     * TODO 'global' here means global to the current the sub-tree
     *
     * @param parentAcc Container to add elements to the parent
     * @param globalAcc Container to add 'global' elements, such as filter expressions
     * @param parentVar
     * @param path
     * @param getChildren
     */
    public void accumulateElements(
            ElementGeneratorContext cxt,
            // TreeDataMap<FacetPath, ElementAcc> facetPathToAcc,
            ElementGroup globalAcc,
            Var parentVar,
            FacetPath path, // TODO This should be the FacetNode and there might be subqueries
            Function<FacetPath, ? extends Iterable<FacetPath>> getChildren) {

//            FacetPath path = ElementGeneratorUtils.cleanPath(rawPath);
//            if (path != rawPath) {
//                accumulate(parentAcc, globalAcc, parentVar, path, getChildren);
//            }
        FacetPath parentPath = path.getParent();
        FacetPath parentEltId = parentPath == null ? null : FacetPathUtils.toElementId(parentPath);
        FacetPath eltId = FacetPathUtils.toElementId(path);

        // Var targetVar = pathMapping.allocate(path);
        Var targetVar = FacetPathMappingImpl.resolveVar(pathMapping, cxt.scope, path).asVar();

        cxt.pathToVar.put(path, targetVar);

        ElementAcc elementAcc = cxt.facetPathToAcc.get(eltId);
        if (elementAcc == null) {
            elementAcc = allocateEltAcc(cxt, parentVar, targetVar, path);
            // The element may exist if eltId is the empty path
            if (!cxt.facetPathToAcc.contains(eltId)) {
                cxt.facetPathToAcc.addItem(parentEltId, eltId);
            }
            cxt.facetPathToAcc.put(eltId, elementAcc);
        }

        // Create the ElementAcc for the path if it hasn't happened yet
        Iterable<FacetPath> children = getChildren.apply(path);
        if (children != null && children.iterator().hasNext()) {
            for (FacetPath subPath : children) {
                // If there is no accumulator for the child then visit it
                accumulateElements(cxt, globalAcc, targetVar, subPath, getChildren);
            }
        }

//            if (!elementAcc.getContainer().isEmpty()) {
//                ElementUtils.copyElements(parentAcc, elementAcc.getResultElement());
//            }

        // Create FILTER elements
        Set<Expr> exprs = cxt.localConstraintIndex.get(path);
        if (!exprs.isEmpty()) { // Wrapped in 'if' for debugging
            createElementsForExprs2(cxt, globalAcc, exprs, false);
        }
    }

    public MappedElement createElement() {
        // TODO Collect all elements from all contexts
        ElementGroup filterGroup = new ElementGroup();
        // ElementGroup elt = new ElementGroup();

        MappedElement result = new MappedElement();
        for (ElementGeneratorContext cxt : scopeToContext.values()) {
            MappedElement part = createElement(cxt, filterGroup);
            result.putAll(part);
        }
        return result;
        // return new MappedElement(cxt.facetPathToAcc, cxt.pathToVar, elt);
    }

    public MappedElement createElement(ElementGeneratorContext cxt, ElementGroup filterGroup) {
        FacetPath rootPath = FacetPath.newAbsolutePath();
        // Var rootVar = pathMapping.allocate(rootPath);
        Var rootVar = cxt.scope.getStartVar(); // FacetPathMappingImpl.resolveVar(pathMapping, cxt.scope, path).asVar();

        // ElementGroup group = new ElementGroup();
        // TreeDataMap<FacetPath, ElementAcc> tree;
        // ElementGroup filterGroup = new ElementGroup();

        // baseConcept.getElements().forEach(group::addElement);
        // Accumulate elements
        for (FacetPath path : cxt.facetTree.getRootItems()) {
            accumulateElements(cxt, filterGroup, rootVar, path, cxt.facetTree::getChildren);
//                accumulate(facetPathToAcc, filterGroup, null, null, facetTree::getChildren);
            // ElementUtils.toElementList(elt).forEach(group::addElement);
            // group.addElement(elt);
        }

                // this.constraintInde


        Element elt = collect(cxt.facetPathToAcc, rootPath);
        elt = ElementUtils.flatten(elt);
        elt = ElementUtils.mergeElements(elt, filterGroup);


        TreeDataMap<ScopedFacetPath, ElementAcc> facetPathToAcc = cxt.facetPathToAcc.mapKeys(facetPath -> ScopedFacetPath.of(cxt.getScope(), facetPath));
        BiMap<ScopedFacetPath, Var> pathToVar = cxt.pathToVar.entrySet().stream()
                .map(e -> new SimpleEntry<>(ScopedFacetPath.of(cxt.getScope(), e.getKey()), e.getValue()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (u, v) -> u, HashBiMap::create));

        // ElementUtils.copyElements(group, filterGroup);

        // Add filters for the constraints
        // Element elt = group.size() == 1 ? group.get(0) : group;

        return new MappedElement(facetPathToAcc, pathToVar, elt);
    }


    public void createElementsForExprs2(ElementGeneratorContext cxt, ElementGroup globalAcc, Collection<Expr> baseExprs, boolean negate) {

        NodeTransform resolveFacetPaths = NodeCustom.createNodeTransform(FacetPath.class, (FacetPath fp) -> {
            return resolve(ScopedFacetPath.of(cxt.getScope(), fp));
        }); //this::resolve);

        Set<Element> result = new LinkedHashSet<>();
        Set<Expr> resolvedExprs = new LinkedHashSet<>();

        // Sort base exprs - absent ones last
        List<Expr> tmp = baseExprs.stream()
                .map(e -> FacetedQueryGenerator.isAbsent(e) ? FacetedQueryGenerator.internalRewriteAbsent(e) : e)
                .collect(Collectors.toList());

        List<Expr> exprs = new ArrayList<>(tmp);
        Collections.sort(exprs, FacetedQueryGenerator::compareAbsent);
        // Resolve the expression
        for(Expr expr : exprs) {

            // TODO We need to add the elements of the paths
            //ExprTransformer.transform(new ExprTransform, expr)
            //Expr resolved = expr.applyNodeTransform(nodeTransform); //ExprTransformer.transform(exprTransform, expr);
            Expr resolved = ExprTransformer.transform(new NodeTransformExpr(resolveFacetPaths), expr);

            resolvedExprs.add(resolved);
        }


        Expr resolvedPathExpr = ExprUtils.orifyBalanced(resolvedExprs);

        if(resolvedPathExpr != null) {
            if(negate) {
                resolvedPathExpr = new E_LogicalNot(resolvedPathExpr);
            }

            // Skip adding constraints that equal TRUE
            if(!NodeValue.TRUE.equals(resolvedPathExpr)) {
                result.add(new ElementFilter(resolvedPathExpr));
            }
        }

        result.forEach(globalAcc::addElement);
    }


    public Var resolve(ScopedFacetPath sfp) {
        return FacetPathMappingImpl.resolveVar(pathMapping, sfp).asVar();
    }

    // Does not seem to be used (yet) - Process all paths referenced by the given expressions
    public void createElementsForExprs(Collection<Expr> baseExprs, boolean negate) {

        Set<Expr> seenExprs = new LinkedHashSet<>();


        NodeTransform resolveFacetPaths = NodeCustom.createNodeTransform(ScopedFacetPath.class, this::resolve);
    //        //NodeTransform xform = NodeTransformLib2.wrapWithNullAsIdentity();
    //
    //
    //            Expr finalExpr = NodeTransformLib.transform(subst, expr);
    //            ElementFilter eltFilter = new ElementFilter(finalExpr);
    //            group.addElement(eltFilter);
    //        }

        Set<Element> result = new LinkedHashSet<>();
        Set<Expr> resolvedExprs = new LinkedHashSet<>();

        // Sort base exprs - absent ones last
        List<Expr> tmp = baseExprs.stream()
                .map(e -> FacetedQueryGenerator.isAbsent(e) ? FacetedQueryGenerator.internalRewriteAbsent(e) : e)
                .collect(Collectors.toList());

        List<Expr> exprs = new ArrayList<>(tmp);
        Collections.sort(exprs, FacetedQueryGenerator::compareAbsent);

        // Collect all mentioned paths so we can getOrCreate their elements


        for(Expr expr : exprs) {
            // Ensure the elements for the paths are created
            allocateElements(expr);
        }

        // Resolve the expression
        for(Expr expr : exprs) {

            // TODO We need to add the elements of the paths
            //ExprTransformer.transform(new ExprTransform, expr)
            //Expr resolved = expr.applyNodeTransform(nodeTransform); //ExprTransformer.transform(exprTransform, expr);
            Expr resolved = ExprTransformer.transform(new NodeTransformExpr(resolveFacetPaths), expr);

            resolvedExprs.add(resolved);
        }


        Expr resolvedPathExpr = ExprUtils.orifyBalanced(resolvedExprs);

        if(resolvedPathExpr != null) {
            if(negate) {
                resolvedPathExpr = new E_LogicalNot(resolvedPathExpr);
            }

            // Skip adding constraints that equal TRUE
            if(!NodeValue.TRUE.equals(resolvedPathExpr)) {
                result.add(new ElementFilter(resolvedPathExpr));
            }
        }

        //BinaryRelation result = new BinaryRelationImpl(ElementUtils.groupIfNeeded(elts), br.getSourceVar(), br.getTargetVar());
        // return result;
    }

    /**
     * Create an Element for the sub-tree of that starts at a given path at the given tree.
     */
    public static Element collect(TreeDataMap<FacetPath, ElementAcc> tree, FacetPath currentPath) {
        ElementAcc eltAcc = tree.get(currentPath);
        Element result;
        if (eltAcc != null) {
            Element elt = eltAcc.getElement();
            // Create the ElementAcc for the path if it hasn't happened yet
            Iterable<FacetPath> children = tree.getChildren(currentPath);
            List<Element> childElts = new ArrayList<>();
            if (children != null && children.iterator().hasNext()) {
                for (FacetPath childPath : children) {
                    Element childElt = collect(tree, childPath);
                    // If there is no accumulator for the child then visit it
                    childElts.add(childElt);
                }
            }
            result = eltAcc.getFactory().apply(elt, childElts);
        } else {
            result = new ElementGroup();
        }
        return result;
    }
}

// protected SetMultimap<ScopedFacetPath, Expr> constraintIndex;

/** Mapping of element paths (FacetPaths with the component set to the TUPLE constant) */
// protected Map<FacetPath, ElementAcc> eltPathToAcc = new LinkedHashMap<>();
// ElementAcc rootEltAcc = ElementAcc.newRoot(); // null; //new ElementAcc();
//protected TreeData<FacetPath> facetTree;
//
///** The FacetPaths on this tree are purely element ids (they reference relations rather than components) */
//protected Set<FacetPath> mandatoryElementIds = new HashSet<>();
//protected TreeDataMap<FacetPath, ElementAcc> facetPathToAcc = new TreeDataMap<>();
//protected Map<FacetPath, Var> pathToVar = new HashMap<>();
//public ScopedFacetPath toElementId(ScopedFacetPath sfp) {
//return sfp.transformPath(FacetPathUtils::toElementId);
//}

//protected ElementGeneratorContext getOrCreateContext(ScopedFacetPath path) {
//VarScope scope = path.getScope();
//
//}


