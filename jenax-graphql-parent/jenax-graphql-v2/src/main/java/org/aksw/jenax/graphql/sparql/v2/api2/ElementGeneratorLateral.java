package org.aksw.jenax.graphql.sparql.v2.api2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jenax.graphql.sparql.v2.model.ElementNode;
import org.aksw.jenax.graphql.sparql.v2.model.ElementNode.JoinLink;
import org.aksw.jenax.graphql.sparql.v2.model.ElementNode.ParentLink;
import org.aksw.jenax.graphql.sparql.v2.util.ElementUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.NodeTransformExpr;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementLateral;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;

public class ElementGeneratorLateral {
    public record ElementMapping(Element element, Map<Node, Map<Var, Var>> stateVarMap) {}

    public static ElementMapping toLateral(ElementNode rootField, Var stateVar) {
        Map<Node, Map<Var, Var>> stateVarMap = new LinkedHashMap<>();
        Map<Node, Map<Var, Var>> outStateOriginalToGlobalMap = new LinkedHashMap<>();
        Element element = toLateral(rootField, List.of(), stateVar, stateVarMap, outStateOriginalToGlobalMap);
        return new ElementMapping(element, stateVarMap);
    }

    public record ElementNodeVarMapping(ElementNode node, Map<Object, Map<Var, Var>> stateVarMap) {}

    public static ElementNodeVarMapping harmonizeVariables(ElementNode elementNode, String prefix) {
        // PathStr basePath = PathStr.newRelativePath(prefix);
        List<String> basePath = List.of(prefix);

        Map<Object, Map<Var, Var>> outStateVarMap = new LinkedHashMap<>();
        Map<Var, Var> parentRenames = new HashMap<>();
        ElementNode node = harmonizeVariables(elementNode, basePath, parentRenames, outStateVarMap);

        return new ElementNodeVarMapping(node, outStateVarMap);
    }

    /**
     * For each node, compute a variable mapping such that all elements can be safely combined into a single graph pattern.
     *
     * @param elementNode
     * @param parentPath
     * @param parentRenames
     * @param outStateVarMap
     * @return
     */
    // TODO This method should be a separate phase in the toLateral() conversion.
    public static ElementNode harmonizeVariables(ElementNode elementNode, List<String> parentPath, Map<Var, Var> parentRenames, Map<Object, Map<Var, Var>> outStateVarMap) {
//        ElementNodeBuilder resultBuilder = ElementNode.newBuilder()
//                .name(elementNode.getName());

        // Construction of the lateral blocks is slightly different for inner nodes and leafs:
        // For inner nodes, the first element in the lateral union harmonizes the variables of the parent
        // For leaf nodes, we can directly expose the bindings with their state
        Collection<Selection> children = elementNode.getSelections();
        List<Var> parentVars = Optional.ofNullable(elementNode.getJoinLink()).map(JoinLink::parentVars).orElse(null);
//        List<Var> parentVars = elementNode.getParentVars();
        String name = elementNode.getName();

        List<String> fieldPath = new ArrayList<>(parentPath);
        fieldPath.add(name);

        String scopeName = fieldPath.size() == 1
                ? "root"
               //  : fieldPath.subpath(1).getSegments().stream().collect(Collectors.joining("_"));
                : fieldPath.subList(1, fieldPath.size()).stream().collect(Collectors.joining("_"));

//        String scopeName = fieldPath.getNameCount() == 1
//                ? "root"
//                : fieldPath.subpath(1).getSegments().stream().collect(Collectors.joining("_"));

        // Compute var mapping for the connective

        Connective connective = elementNode.getConnective();

        Element element = connective.getElement();
        Set<Var> mentionedVars = VarHelper.vars(element);

        // Set<Var> projVars = new LinkedHashSet<>();

        Map<Var, Var> originalToGlobal = outStateVarMap.computeIfAbsent(scopeName, k -> new LinkedHashMap<>()); // new LinkedHashMap<>();

        // Map the connectVars to the parentVars
        if (parentVars != null) {
            List<Var> connectVars = connective.getConnectVars();
            for (int i = 0; i < connectVars.size(); ++i) {
                Var connectVar = connectVars.get(i);
                Var parentVar = parentVars.get(i);
                Var globalVar = originalToGlobal.computeIfAbsent(connectVar, v -> parentRenames.getOrDefault(parentVar, parentVar));
                // projVars.add(globalVar);
            }
        }

        // Map all remaining variables into the current scope
        for (Var var : mentionedVars) {
            originalToGlobal.computeIfAbsent(var, v -> Var.alloc(scopeName + "_" + v.getName()));
        }

        Connective globalConnective = connective.applyNodeTransform(new NodeTransformSubst(originalToGlobal));
        ElementNode resultNode = ElementNode.of(elementNode.getLabel(), globalConnective);
        // resultBuilder.connective(globalConnective);

        List<Var> globalParentVars = parentVars != null
                ? parentVars.stream().map(originalToGlobal::get).toList()
                : null;

        // ElementNode resultNode = resultBuilder.build();

        // If there are children then generate a LATERAL BLOCK
        for (Selection selection : elementNode.getSelections()) {
            if (selection instanceof ElementNode f) {
                ElementNode childBuilder = harmonizeVariables(f, fieldPath, originalToGlobal, outStateVarMap);
                // resultMemberBuilder.addSelection(childBuilder);
                resultNode.addChild(globalParentVars, childBuilder);
            }
        }

        return resultNode;
    }

    public static Var resolveAncestorVar(ElementNode elementNode, Map<Node, Map<Var, Var>> outStateVarMap, Var var) {
        String id = elementNode.getIdentifier();
        Node idNode = NodeFactory.createLiteralString(id);
        Map<Var, Var> localToGlobal = outStateVarMap.get(idNode);
        Objects.requireNonNull(localToGlobal, "Unexpectedly found no local-to-global variable mapping for state: " + idNode);

        Var result = localToGlobal.get(var);
        if (result == null) {
            ParentLink parentLink = elementNode.getParentLink();
            result = parentLink == null ? null : resolveAncestorVar(parentLink.parent(), outStateVarMap, var);
        }
        return result;
    }


    public static Map<Var, Var> resolveVarMap(ElementNode elementNode, Map<Node, Map<Var, Var>> outStateVarMap, Expr expr) {
        Set<Var> vars = expr.getVarsMentioned();
        Map<Var, Var> localToGlobal = new HashMap<>();
        for (Var var : vars) {
            Var resolvedVar = resolveAncestorVar(elementNode, outStateVarMap, var);
            if (resolvedVar == null) {
                throw new RuntimeException("Could not resolve variable: " + var + " in expression: " + expr);
            }
            localToGlobal.put(var, resolvedVar);
        }
        return localToGlobal;
    }

    public static Expr resolveLocalVarsInExpr(ElementNode elementNode, Map<Node, Map<Var, Var>> outStateVarMap, Expr expr) {
        Map<Var, Var> localToGlobal = resolveVarMap(elementNode, outStateVarMap, expr);
        Expr result = ExprTransformer.transform(new NodeTransformExpr(new NodeTransformSubst(localToGlobal)), expr);
        return result;
    }

    /**
     *
     * @param node
     * @param parentPath
     * @param parentRenames
     * @param discriminatorVar
     * @param outStateVarMap For each state the mapping of the original var to the renamed var. The rationale is: Access to the original variable needs to be remapped to the renamed one.
     * @return
     */
    public static Element toLateral(ElementNode node, List<String> parentPath, Var discriminatorVar,
            Map<Node, Map<Var, Var>> outStateVarMap, Map<Node, Map<Var, Var>> outStateOriginalToGlobalMap) {

        // Construction of the lateral blocks is slightly different for inner nodes and leafs:
        // For inner nodes, the first element in the lateral union harmonizes the variables of the parent
        // For leaf nodes, we can directly expose the bindings with their state
        Collection<Selection> children = node.getSelections();
        boolean isLeaf = children.isEmpty();

        JoinLink joinLink = node.getJoinLink();
        // List<Var> parentVars = Optional.ofNullable(node.getJoinLink()).map(JoinLink::parentVars).orElse(null);
        //List<Var> parentVars = node.getParentVars();
        String name = node.getName();

        // PathStr fieldPath = parentPath.resolve(name);
        List<String> fieldPath = new ArrayList<>(parentPath);
        fieldPath.add(name);

        String scopeName = fieldPath.size() == 1
                ? "root"
               //  : fieldPath.subpath(1).getSegments().stream().collect(Collectors.joining("_"));
                : fieldPath.subList(1, fieldPath.size()).stream().collect(Collectors.joining("_"));

        // NodeValue discriminatorValue = NodeValue.makeNodeString(scopeName);
        String stateId = node.getIdentifier();
        if (stateId == null) {
            throw new IllegalArgumentException("Node had null for its identifier");
        }
        Node stateIdNode = NodeFactory.createLiteralString(stateId);

        NodeValue discriminatorValue = NodeValue.makeNode(stateIdNode);

        // Compute var mapping for the connective

        Connective connective = node.getConnective();

        Element element = connective.getElement();
        Set<Var> mentionedVars = VarHelper.vars(element);

        Set<Var> projVars = new LinkedHashSet<>();
        projVars.add(discriminatorVar);

        Map<Var, Var> originalToGlobal = outStateOriginalToGlobalMap.computeIfAbsent(stateIdNode, k -> new LinkedHashMap<>());
        // Map<Var, Var> originalToGlobal = new LinkedHashMap<>();

        // Map the connectVars to the parentVars
        if (joinLink != null) {
            // List<Var> connectVars = connective.getConnectVars();
            for (int i = 0; i < joinLink.size(); ++i) {
                Var connectVar = joinLink.childVars().get(i);
                Var parentVar = joinLink.parentVars().get(i);

                Var globalVar = resolveAncestorVar(node, outStateOriginalToGlobalMap, parentVar);
                originalToGlobal.computeIfAbsent(connectVar, v -> globalVar);

                // Var globalVar = originalToGlobal.computeIfAbsent(connectVar, v -> parentRenames.getOrDefault(parentVar, parentVar));
                projVars.add(globalVar);
            }
        }

        // Map all remaining variables into the current scope
        for (Var var : mentionedVars) {
            originalToGlobal.computeIfAbsent(var, v -> Var.alloc(scopeName + "_" + v.getName()));
        }

        // The mapping which original variable accesses which enumerated one
        // Map<Var, Var> originalToEnum = new LinkedHashMap<>();


//        for (Entry<Var, Var> e : originalToGlobal.entrySet()) {
//            Var originalVar = e.getKey();
//            Var globalVar = e.getValue();
//            Var enumVar = Var.alloc("v_" + i);
//            globalToEnum.put(globalVar, enumVar);
//            originalToEnum.put(originalVar, enumVar);
//
//            // enumToOriginal.put(enumVar, originalVar);
//
//            ++i;
//        }


        ElementGroup group = new ElementGroup();

        if (isLeaf) {
            group.addElement(new ElementBind(discriminatorVar, discriminatorValue));
        }

        Connective globalConnective = connective.applyNodeTransform(new NodeTransformSubst(originalToGlobal));
        ElementUtils.copyElements(group, globalConnective.getElement());


        // Add BIND expressions
        // TODO The defined variables (expr AS ?definedVar) also need to become part of the orginitalToGlobal map.
        node.getBinds().forEachExpr((v, e) -> {
            Var resolvedVar = originalToGlobal.computeIfAbsent(v, vv -> Var.alloc(scopeName + "_" + vv.getName()));
            Expr resolvedExpr = resolveLocalVarsInExpr(node, outStateOriginalToGlobalMap, e);
            group.addElement(new ElementBind(resolvedVar, resolvedExpr));
        });


        // Map all unique variables to enumerated ones
        int i = 0;
        Map<Var, Var> globalToEnum = new LinkedHashMap<>();

        // Expose the state variable
        Map<Var, Var> originalToEnum = outStateVarMap.computeIfAbsent(stateIdNode, k -> new LinkedHashMap<>());
        originalToEnum.put(discriminatorVar, discriminatorVar);

        // Create the enum vars
        for (Entry<Var, Var> e : originalToGlobal.entrySet()) {
            Var originalVar = e.getKey();
            Var globalVar = e.getValue();
            Var enumVar = Var.alloc("v_" + i);
            globalToEnum.put(globalVar, enumVar);
            originalToEnum.put(originalVar, enumVar);
            // enumToOriginal.put(enumVar, originalVar);
            ++i;
        }

        // ISSUE: We can't optimize projections here (yet) because we don't know which variables are referenced.
        projVars.addAll(originalToEnum.values());
        // System.out.println(projVars);


        if (isLeaf) {
            // Generate BIND blocks for enums
            globalToEnum.forEach((from, to) -> group.addElement(new ElementBind(to, new ExprVar(from))));
        }

        Element primary = group;
        for (ElementTransform transform : node.getLocalTransforms()) {
            Element tmp = transform.apply(primary);
            primary = tmp;
        }

        // Slice happens after local transforms
        primary = applySlice(primary, node.getOffset(), node.getLimit());

        // If there are children then generate a LATERAL BLOCK
        ElementGroup grp = primary instanceof ElementGroup g ? g : ElementUtils.createElementGroup(primary);
        if (!isLeaf) {
            // Ensure that the whole group is part of LATERAL's right hand side.
            if (grp.size() > 1) {
                ElementGroup tmp = new ElementGroup();
                tmp.addElement(grp);
                grp = tmp;
            }

            List<Element> members = new ArrayList<>();

            // Create a member that exposes the canonical parent state
            {
                // Add the discriminator column
                List<Element> headElts = new ArrayList<>();
                headElts.add(new ElementBind(discriminatorVar, discriminatorValue));

                // Generate BIND blocks for enums
                globalToEnum.forEach((from, to) -> headElts.add(new ElementBind(to, new ExprVar(from))));
                Element head = ElementUtils.groupIfNeeded(headElts);

                members.add(head);
            }

            for (Selection selection : node.getSelections()) {
                if (selection instanceof ElementNode f) {
                    Element contrib = toLateral(f, fieldPath, discriminatorVar, outStateVarMap, outStateOriginalToGlobalMap);
                    members.add(contrib);
                }
            }

            Element union = ElementUtils.unionIfNeeded(members);
            Element lateral = new ElementLateral(union);
            grp.addElement(lateral);
        } else {
            // Wrap the group with a proper projection
        }

        Element result = grp;
        for (ElementTransform transform : node.getTreeTransforms()) {
            Element tmp = transform.apply(result);
            result = tmp;
        }

        return result;
    }

    public static Element applySlice(Element elt, Long offset, Long limit) {
        Element result = elt;
        if (limit != null || offset != null) {
            Query query = QueryUtils.elementToQuery(elt);
            if (limit != null) {
                query.setLimit(limit);
            }
            if (offset != null) {
                query.setOffset(offset);
            }
            result = new ElementSubQuery(query);
        }
        return result;
    }
}
