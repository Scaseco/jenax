package org.aksw.jenax.facete.treequery2.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.commons.util.direction.Direction;
import org.aksw.facete.v3.api.FacetPathMapping;
import org.aksw.facete.v3.api.TreeData;
import org.aksw.facete.v4.impl.ElementGeneratorWorker;
import org.aksw.facete.v4.impl.MappedElement;
import org.aksw.facete.v4.impl.PropertyResolver;
import org.aksw.jena_sparql_api.rx.entity.engine.EntityQueryRx;
import org.aksw.jena_sparql_api.rx.entity.model.EntityBaseQuery;
import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryImpl;
import org.aksw.jena_sparql_api.rx.entity.model.EntityTemplateImpl;
import org.aksw.jena_sparql_api.schema.ShUtils;
import org.aksw.jenax.arq.util.node.NodeCustom;
import org.aksw.jenax.arq.util.node.NodeTransformLib2;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.dataaccess.sparql.execution.factory.query.QueryExecutionFactories;
import org.aksw.jenax.facete.treequery2.api.ConstraintNode;
import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.facete.treequery2.api.RelationQuery;
import org.aksw.jenax.facete.treequery2.api.ScopedFacetPath;
import org.aksw.jenax.facete.treequery2.api.ScopedVar;
import org.aksw.jenax.model.shacl.domain.ShNodeShape;
import org.aksw.jenax.model.shacl.domain.ShPropertyShape;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.path.PathUtils;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.SortCondition;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementLateral;
import org.apache.jena.sparql.syntax.ElementService;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sys.JenaSystem;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.SetMultimap;


public class ElementGeneratorLateral {

    static { JenaSystem.init(); }
    // protected PropertyResolver propertyResolver;


    public static Element toElement(RelationQuery current) {
        return new ElementGeneratorLateral().createElement(current, null);
    }

    public static Query toQuery(NodeQuery nodeQuery) {
        return toQuery(nodeQuery.relationQuery());
    }

    public static Query toQuery(RelationQuery relationQuery) {
        Element elt = toElement(relationQuery);
        List<NodeQuery> roots = relationQuery.roots();
//        if (rootVars.size() != 1) {
//        	throw new RuntimeException("Only a single designated root variable expected");
//        }
        // Convention: First variable is always the root
        Var rootVar = roots.get(0).var();

        Query query = new Query();
        query.setConstructTemplate(new Template(new QuadAcc(Arrays.asList(Quad.create(rootVar, Vars.x, Vars.y, Vars.z)))));
        query.setQueryConstructType();
        query.setQueryPattern(elt);

        return query;
    }

    /**
     * SELECT (?key1 ... ?keyN)    ?s ?p ?o
     *
     * (base relation with ?key1 ... ?keyN and roots ?root1 ... ?rootM) (the sets of keys and roots are not required to be disjoint)
     * LATERAL { # For each unique key combination
     *     { # Union over all roots
     *       BIND("root1" AS ?root)
     *       ?root1 :p1 ?b .
     *       LATERAL {
     *           { BIND(?a AS ?s) BIND(:p1 AS ?p) BIND(?b AS ?o) }
     *         UNION
     *           {
     *             ?b :p2 ?c.
     *             LATERAL {
     *                 { BIND(?b AS ?s) BIND(:p2 AS ?p) BIND(?c AS ?o) }
     *               UNION
     *                 {
     *
     *                 }
     *             }
     *           }
     *         }
     *     }
     *   UNION
     *     {
     *     }
     *  }
     *}
     */
    public static Element createElement(TreeData<FacetPath> tree, String rootVar, FacetPathMapping pathMapping) {
        // createElement(tree, current);
        // pathMapping.allocate(null)
        throw new UnsupportedOperationException("finish this");
    }
//
//    public Element createElement(NodeQueryOld current) {
//        // worker.allocateElement(null)
//        // UnaryRelation baseConcept = new Concept(baseRelation.getElement(), rootVar);
//        Var rootVar = Var.alloc("root");
//        Generator<Var> varGen = GeneratorFromFunction.createInt().map(i -> Var.alloc("vv" + i));
//        DynamicInjectiveFunction<FacetPath, Var> ifn = DynamicInjectiveFunction.of(varGen);
//        ifn.getMap().put(FacetPath.newAbsolutePath(), rootVar);
//
//        FacetPathMapping fpm = ifn::apply;
//        ElementGenerator eltGen = new ElementGenerator(fpm, HashMultimap.create(), FacetPath.newAbsolutePath());
//
//        ElementGeneratorWorker worker = eltGen.createWorker();
//
//        // Traverser.forTree(treeData::getChildren).depthFirstPreOrder(treeData.getRootItems()).forEach(eltGen::addPath);
//        Element result = createElementOld(worker, rootVar, current);
//        // MappedElement result = worker.createElement();
//        return result;
//    }

    /**
     * The paths in the tree is what is being projected.
     *
     * @param tree
     * @param current
     * @return
     */
//    public  Element createElementOld(ElementGeneratorWorker worker, Var parentVar, NodeQueryOld current) {
//        FacetPath path = current.getPath();
//        FacetStep step = ListUtils.lastOrNull(path.getSegments());
//
//        List<Element> unionMembers = new ArrayList<>();
//
//        Var targetVar;
//        Element nodeElement;
//        if (step != null) {
//            // Node p = step.getNode();
//            // Create the element for this node
//            targetVar = worker.getPathMapping().allocate(path);
//            Var predicateVar = worker.getPathMapping().allocate(path.resolveSibling(FacetStep.of(step.getNode(), step.getDirection(), step.getAlias(), FacetStep.PREDICATE)));
//
//            nodeElement = worker.createElementForLastStep(parentVar, targetVar, path); // createElement(worker, targetVar, child);
//
//            Long limit = current.limit();
//            Long offset = current.offset();
//            if (limit != null || offset != null) {
//                Query subQuery = new Query();
//                subQuery.setQuerySelectType();
//                subQuery.addProjectVars(Arrays.asList(parentVar, targetVar));
//                subQuery.setLimit(limit == null ? Query.NOLIMIT : limit);
//                subQuery.setOffset(offset == null ? Query.NOLIMIT : offset);
//                subQuery.setQueryPattern(nodeElement);
//                nodeElement = new ElementSubQuery(subQuery);
//            }
//
//            boolean applyCache = false;
//            if (applyCache) {
//                nodeElement = new ElementService("bulk+10:cache:", nodeElement);
//            }
//
//            // If there is limit, slice, filter or order then create an appropriate sub-query
//            // Bind the parent variable to '?s'
//            ElementBind bindS = new ElementBind(Vars.s, new ExprVar(parentVar));
//
//            // Bind the predicate to ?p
//            ElementBind bindP = new ElementBind(Vars.p, NodeValue.makeString(predicateVar.getName()));
//
//            // Bind element's target to ?o
//            ElementBind bindO = new ElementBind(Vars.o, new ExprVar(targetVar));
//
//            ElementGroup bindSpoGroup = new ElementGroup();
//            bindSpoGroup.addElement(bindS);
//            bindSpoGroup.addElement(bindP);
//            bindSpoGroup.addElement(bindO);
//
//
//            // AggBuilder.map
//
//
//            // Add the bindSpoGroup as the first union member
//            unionMembers.add(bindSpoGroup);
//        } else {
//            nodeElement = new ElementGroup();
//            targetVar = parentVar;
//        }
//
//        // Add any children
//        Collection<NodeQueryOld> children = current.getChildren();
//        for (NodeQueryOld child : children) {
//            Element elt = createElementOld(worker, targetVar, child);
//            unionMembers.add(elt);
//        }
//        Element union = ElementUtils.unionIfNeeded(unionMembers);
//
//        // Create the lateral group for this node
//        ElementLateral lateralUnion = new ElementLateral(union);
//
//        // Create the group for this node
//        ElementGroup group = new ElementGroup();
//        ElementUtils.copyElements(group, nodeElement);
//        ElementUtils.copyElements(group, lateralUnion);
//        Element result = ElementUtils.flatten(group);
//        return result;
//    }

    // public static SetMultimap<ScopedFacetPath, Expr> createConstraintIndex(RelationQuery relationQuery) {
    public static Set<Expr> createScopedConstraintExprs(RelationQuery relationQuery) {
        FacetConstraints<ConstraintNode<NodeQuery>> constraints = relationQuery.getFacetConstraints();
        Collection<Expr> rawExprs = constraints.getExprs();
        NodeTransform constraintTransform = NodeCustom.mapValue((ConstraintNode<NodeQuery> cn) -> ConstraintNode.toScopedFacetPath(cn));
        Set<Expr> result = rawExprs.stream()
                .map(e -> e.applyNodeTransform(constraintTransform))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        // SetMultimap<ScopedFacetPath, Expr> result = FacetConstraints.createConstraintIndex(exprs);
        return result;
    }

    public Element createElement(RelationQuery current) {
        return createElement(current, null);
    }

    public Element createElement(RelationQuery current, FacetStep reachingStep) {
        // FacetPath path = current.getPath();
        // FacetStep step = ListUtils.lastOrNull(path.getSegments());

        List<Element> unionMembers = new ArrayList<>();

        Var parentVar = current.source().var();

        Var targetVar = current.target().var();
        Element nodeElement;
        // if (step != null) {
            // Node p = step.getNode();
            // Create the element for this node

        Relation relation = current.getRelation();
        nodeElement = relation.getElement();

        Long limit = current.limit();
        Long offset = current.offset();
        // int sortDirection = current.get
        List<SortCondition> sortConditions = current.getSortConditions();

        // Handle constraints
        Set<Expr> constraintExprs = createScopedConstraintExprs(current);
        SetMultimap<ScopedFacetPath, Expr> constraintIndex = FacetConstraints.createConstraintIndex(constraintExprs);


        // TreeData<FacetPath> facetTree = new TreeData<>(); // Empty tree because we rely on the constraints
        PropertyResolver propertyResolver = current.getContext().getPropertyResolver();

//            String parentScopeNode = Optional.ofNullable(current.getParentNode())
//                    .map(NodeQuery::relationQuery).map(RelationQuery::getScopeBaseName).orElse(null);
//
//            ScopeNode scopeNode = new ScopeNode(current.getScopeBaseName(), current.target().var());
        org.aksw.jenax.facete.treequery2.api.FacetPathMapping pathMapping = current.getContext().getPathMapping(); // new FacetPathMappingImpl();

        TreeData<ScopedFacetPath> treeData = new TreeData<>();
        for (ScopedFacetPath key : constraintIndex.keySet()) {
            treeData.putItem(key, ScopedFacetPath::getParent);
        }

        // Extract sort conditions
        for (SortCondition sc : sortConditions) {
            Set<ConstraintNode> nodes = NodeCustom.mentionedValues(sc.getExpression(), ConstraintNode.class);
            for (ConstraintNode<NodeQuery> node : nodes) {
                ScopedFacetPath pathContrib = ConstraintNode.toScopedFacetPath(node);
                treeData.putItem(pathContrib, ScopedFacetPath::getParent);
            }
        }

        if (!constraintIndex.isEmpty()) {
            // System.out.println("Constraints: " + constraintIndex);
        }

        ElementGeneratorWorker eltWorker = new ElementGeneratorWorker(treeData, constraintIndex, pathMapping, propertyResolver);


        // eltWorker.setConstraintIndex(constraintIndex);

        // TODO The constraint index is not processed into a facet tree here yet
        // TODO Why do we get an NPE with the root path and an empty tree?
        // (in principle: trees are rooted in null, so the empty root path maps to null; but what's the clean
        // way to fix this?)

        MappedElement constraintEltAcc = eltWorker.createElement();
        Element constraintElt = constraintEltAcc.getElement();
        // System.out.println("Elt: " + constraintElt);

        nodeElement = ElementUtils.mergeElements(nodeElement, constraintElt);

        // ElementGenerator eltGen = new ElementGenerator(pathMapping, constraintIndex, null);
        // new ElementGeneratorWorker(facetTree, constraintIndex, scopeNode, propertyResolver);

        if (limit != null || offset != null || !sortConditions.isEmpty()) {
            Query subQuery = new Query();
            subQuery.setQuerySelectType();
            subQuery.addProjectVars(Arrays.asList(parentVar, targetVar));
            subQuery.setLimit(limit == null ? Query.NOLIMIT : limit);
            subQuery.setOffset(offset == null ? Query.NOLIMIT : offset);
            subQuery.setQueryPattern(nodeElement);

            // FIXME In general we need to resolve path references!

            NodeTransform xform = NodeCustom.createNodeTransform((ConstraintNode cn) -> {
                ScopedFacetPath spf = ConstraintNode.toScopedFacetPath(cn);
                Var v = FacetPathMappingImpl.resolveVar(pathMapping, spf).asVar();
                return v;
            });
            for (SortCondition sc : sortConditions) {
                SortCondition resolvedSc = NodeTransformLib2.transform(xform, sc);
                subQuery.addOrderBy(resolvedSc);
            }

            nodeElement = new ElementSubQuery(subQuery);
        }

        boolean applyCache = false;
        if (applyCache) {
            nodeElement = new ElementService("bulk+10:cache:", nodeElement);
        }

        if (reachingStep != null) {

            Var s = current.source().var();
            Node p;
            Var o = targetVar;

            if (reachingStep.getDirection().equals(Direction.BACKWARD)) {
                o = s;
                s = targetVar;
            }


            if (relation.getVars().size() == 2) {
                // System.out.println("Relation: " + relation);
                // FacetStep facetStep = current.getParentNode().reachingStep();
                // FacetStep facetStep = current.getReachingStep();
                // FacetPath facetPath = current.getParentNode().getFacetPath();
                // FacetStep facetStep = facetPath.getFileName().toSegment();
                p = reachingStep.getNode();
            } else {
                p = current.target().var();
            }

            // If there is limit, slice, filter or order then create an appropriate sub-query
            // Bind the parent variable to '?s'



            ElementGroup bindSpoGroup = new ElementGroup();
            ElementBind bindS = new ElementBind(Vars.x, new ExprVar(s));
            bindSpoGroup.addElement(bindS);

            // Bind the predicate to ?p
            ElementBind bindP = new ElementBind(Vars.y, ExprLib.nodeToExpr(p));
            bindSpoGroup.addElement(bindP);

            // Bind element's target to ?o
            ElementBind bindO = new ElementBind(Vars.z, new ExprVar(o));
            bindSpoGroup.addElement(bindO);

            // Add the bindSpoGroup as the first union member
            unionMembers.add(bindSpoGroup);
        }
//        } else {
//            nodeElement = new ElementGroup();
//            targetVar = parentVar;
//        }

        // Add any children
        Collection<NodeQuery> children = current.roots();
        for (NodeQuery child : children) {
            for (RelationQuery subRq : child.children().values()) {
                FacetStep stepToChild = subRq.getReachingStep();
                Element elt = createElement(subRq, stepToChild);
                unionMembers.add(elt);
            }
        }

        // Create the group for this node
        ElementGroup group = new ElementGroup();
        ElementUtils.copyElements(group, nodeElement);
        if (!unionMembers.isEmpty()) {
            Element union = ElementUtils.unionIfNeeded(unionMembers);
            // Create the lateral group for this node
            ElementLateral lateralUnion = new ElementLateral(union);
            ElementUtils.copyElements(group, lateralUnion);
        }
        Element result = ElementUtils.flatten(group);
        return result;
    }


    /**
     *
     * Traversing along the empty path from a relationQuery's variable returns that variable again.
     * Further traversals allocate scoped variables.
     */
    public static Function<ConstraintNode<NodeQuery>, Var> resolveScopeName(org.aksw.jenax.facete.treequery2.api.FacetPathMapping facetPathMapping) {
        // String name = facetPathMapping.allocate(facetPath);
        // return baseScopeName + "_" + name;
        return (ConstraintNode<NodeQuery> constraintNode) -> {
            FacetPath facetPath = constraintNode.getFacetPath();
            NodeQuery nq = constraintNode.getRoot();
            Var var = nq.var();
            String baseScopeName = nq.relationQuery().getScopeBaseName();
            ScopedVar sc = FacetPathMappingImpl.resolveVar(facetPathMapping, baseScopeName, var, facetPath);
            Var r = sc.asVar();
            return r;
        };
    }

    // TODO We now need to add a facet-path to variable name mapping
    public static void toNodeQuery(NodeQuery nodeQuery, ShNodeShape nodeShape) {
        for (ShPropertyShape propertyShape : nodeShape.getProperties()) {
            Resource pathResource = propertyShape.getPath();
            Path sparqlPath = ShUtils.assemblePath(pathResource);
            System.err.println("GOT PATH: " + sparqlPath);

//            if (sparqlPath.toString().contains("sqlQuery")) {
//                System.err.println("DEBUG POINT");
//            }


            BiMap<Node, Path> iriToPath = HashBiMap.create();
            try {
                List<P_Path0> steps = PathUtils.toList(sparqlPath);
                NodeQuery current = nodeQuery;
                for (P_Path0 step : steps) {
                    FacetStep facetStep = FacetStep.of(step.getNode(), Direction.ofFwd(step.isForward()), null, FacetStep.TARGET);
                    current = current.getOrCreateChild(facetStep);
                }
            } catch (UnsupportedOperationException e) {
                Generator<Node> pGen = Generator.create("urn:p")
                        .map(NodeFactory::createURI).filterDrop(iriToPath::containsKey);
                Node iri = iriToPath.inverse().computeIfAbsent(sparqlPath, sp -> pGen.next());
                nodeQuery.getOrCreateChild(FacetStep.fwd(iri));
                Element elt = ElementUtils.createElementPath(Vars.s, sparqlPath, Vars.o);
                // FIXME Register the iri with sparql path in the nodeQuery context
            }
        }

        for (ShNodeShape ns : nodeShape.getAnd()) {
            toNodeQuery(nodeQuery, ns);
        }

        for (ShNodeShape ns : nodeShape.getOr()) {
            toNodeQuery(nodeQuery, ns);
        }

        for (ShNodeShape ns : nodeShape.getXone()) {
            toNodeQuery(nodeQuery, ns);
        }

        // Include NOT shapes? They should not be present anyway, but displaying them
        // might be useful to spot data quality issues
        for (ShNodeShape ns : nodeShape.getNot()) {
            toNodeQuery(nodeQuery, ns);
        }
    }

    /**
     * Util method to extract all properties regardless of the
     * Logical Constraint Components sh:not, sh:and, sh:or and sh:xone
     *
     * TODO Introduce a visitor?
     *
     * https://www.w3.org/TR/shacl/#shapes-recursion
     */
    public static List<ShPropertyShape> getPropertyShapes(ShNodeShape nodeShape) {
        for (ShPropertyShape propertyShape : nodeShape.getProperties()) {
            Resource pathResource = propertyShape.getPath();
            Path sparqlPath = ShUtils.assemblePath(pathResource);
            System.err.println("GOT PATH: " + sparqlPath);
        }

        //

        return null;
    }

    public static void main2(String[] args) {
        Query concept = QueryFactory.create("SELECT ?s ?p ?o { ?s ?p ?o }");

//        boolean materialize = false;
//        if (materialize) {
//            Query c = concept;
//            Table table = QueryExecutionUtils.execSelectTable(() -> QueryExecutionFactory.create(c, model));
//
//            Query tmp = QueryFactory.create("SELECT DISTINCT ?s {}");
//            tmp.setQueryPattern(new ElementData(table.getVars(), Lists.newArrayList(table.rows())));
//            concept = tmp;
//        }

        // raw.setValuesDataBlock(table.getVars(), Lists.newArrayList(table.rows()));
        EntityBaseQuery ebq = new EntityBaseQuery(Arrays.asList(Vars.s, Vars.o), new EntityTemplateImpl(), concept);
        System.out.println(ebq);

        EntityQueryImpl eq = new EntityQueryImpl();
        eq.setBaseQuery(ebq);
        System.out.println(eq);

        RDFConnection conn = RDFConnection.connect(DatasetFactory.create());
        EntityQueryRx.execConstructEntitiesNg(QueryExecutionFactories.of(conn), eq).toList().blockingGet();

    }
}
