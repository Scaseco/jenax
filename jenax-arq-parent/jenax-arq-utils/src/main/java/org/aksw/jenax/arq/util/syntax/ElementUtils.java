package org.aksw.jenax.arq.util.syntax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.collections.MapUtils;
import org.aksw.commons.collections.generator.Generator;
import org.aksw.jenax.arq.util.expr.CnfUtils;
import org.aksw.jenax.arq.util.node.NodeTransformCollectNodes;
import org.aksw.jenax.arq.util.node.NodeTransformRenameMap;
import org.aksw.jenax.arq.util.var.VarGeneratorBlacklist;
import org.aksw.jenax.arq.util.var.Vars;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ExprTransformNodeElement;

import com.google.common.collect.Iterables;
//import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
//import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer;
//import org.apache.jena.sparql.syntax.syntaxtransform.ExprTransformNodeElement;

//import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
//import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer;
//import org.apache.jena.sparql.syntax.syntaxtransform.ExprTransformNodeElement;


public class ElementUtils {

//    public static Generator<Var> freshVars(Element element){
//        Collection<Var> blacklistVars = PatternVars.vars(element);
//
//        Generator<Var> gen = VarGeneratorImpl2.create("v");
//        Generator<Var> result = new VarGeneratorBlacklist(gen, blacklistVars);
//
//        return result;
//    }


    public static List<Element> getSubElements(Element element) {
        return ElementVisitorGetSubElements.getSubElements(element);
    }

    public static Element applyOpTransform(Element elt, Function<? super Op, ? extends Op> transform) {
        Op beforeOp = Algebra.compile(elt);
        Op afterOp = transform.apply(beforeOp);
        Query query = OpAsQuery.asQuery(afterOp);
        Element r = query.getQueryPattern();
        return r;
    }

    // PatternVars only returns visible vars, this returns all mentioned vars
    public static Set<Var> getVarsMentioned(Element e) {
        NodeTransformCollectNodes tmp = new NodeTransformCollectNodes();
        ElementUtils.applyNodeTransform(e, tmp);

        Set<Node> nodes = tmp.getNodes();
        Set<Var> result = nodes.stream()
                .filter(Node::isVariable)
                .map(n -> (Var)n)
                .collect(Collectors.toSet());

        return result;
    }


    public static ElementTriplesBlock createElementTriple(Triple ... triples) {
        return createElementTriple(Arrays.asList(triples));
    }

    public static ElementTriplesBlock createElementTriple(Iterable<Triple> triples) {
        BasicPattern bgp = new BasicPattern();
        triples.forEach(bgp::add);
        ElementTriplesBlock result = new ElementTriplesBlock(bgp);
        return result;
    }

    public static ElementTriplesBlock createElementTriple(Node s, Node p, Node o) {
        return createElement(new Triple(s, p, o));
    }


    public static ElementPathBlock createElementPath(Node s, Path p, Node o) {
        ElementPathBlock result = createElementPath(new TriplePath(s, p, o));
        return result;
    }

    public static ElementPathBlock createElementPath(TriplePath ... tps) {
        ElementPathBlock result = createElementPath(Arrays.asList(tps));
        return result;
    }

    public static ElementPathBlock createElementPath(Iterable<TriplePath> it) {
        ElementPathBlock result = new ElementPathBlock();
        for(TriplePath tp : it) {
            result.addTriple(tp);
        }
        return result;
    }

    public static ElementTriplesBlock createElement(Triple triple) {
        BasicPattern bgp = new BasicPattern();
        bgp.add(triple);
        ElementTriplesBlock result = new ElementTriplesBlock(bgp);
        return result;
    }

    public static Element createElement(Quad quad) {
        Element tripleEl = createElement(quad.asTriple());

        Element result = Quad.isDefaultGraph(quad.getGraph())
                ? tripleEl
                : new ElementNamedGraph(quad.getGraph(), tripleEl);

        return result;
    }

    public static ElementPathBlock createElement(TriplePath triplePath) {
        ElementPathBlock result = new ElementPathBlock();
        result.addTriplePath(triplePath);
        return result;
    }


    public static List<Triple> extractTriples(Element e) {
        List<Triple> result = new ArrayList<Triple>();
        extractTriples(e, result);
        return result;
    }


    //public static Element join()

    public static Triple extractTriple(Element e) {
        //Node result = null;
        Triple result = null;

        if(e instanceof ElementFilter) {
            ElementFilter x = (ElementFilter)e;
            Expr expr = x.getExpr();
            Set<Set<Expr>> cnf = CnfUtils.toSetCnf(expr);
            Map<Var, Node> map = CnfUtils.getConstants(cnf, true);

            //Node g = MapUtils.getOrElse(map, Vars.g, Node.ANY);
            Node s = MapUtils.getOrElse(map, Vars.s, Node.ANY);
            Node p = MapUtils.getOrElse(map, Vars.p, Node.ANY);
            Node o = MapUtils.getOrElse(map, Vars.o, Node.ANY);
            result = new Triple(s, p, o);
        } else {
            List<Triple> triples = extractTriples(e);
            if(triples.size() == 1) {
                result = triples.get(0);
                //Triple t = triples.get(0);
                //result = t.getPredicate();
            }
        }
        return result;
    }

    /** Use toGraph() which uses proper visitors to extract triples */
    @Deprecated
    public static void extractTriples(Element e, List<Triple> result) {

        if(e instanceof ElementGroup) {
            ElementGroup g = (ElementGroup)e;
            for(Element item : g.getElements()) {
                extractTriples(item, result);
            }
        } else if(e instanceof ElementTriplesBlock) {
            ElementTriplesBlock b = (ElementTriplesBlock)e;
            List<Triple> triples = b.getPattern().getList();
            result.addAll(triples);
        }
    }

    public static DatasetGraph toDataset(Element elt) {
        return toDataset(elt, DatasetGraphFactory.create());
    }

    public static <T extends DatasetGraph> T toDataset(Element elt, T acc) {
        ElementVisitorDatasetGraph visitor = new ElementVisitorDatasetGraph(acc);
        ElementWalker.walk(elt, visitor);
        return acc;
    }

    public static Graph toGraph(Element elt) {
        return toGraph(elt, GraphFactory.createDefaultGraph());
    }

    public static <T extends Graph> T toGraph(Element elt, T acc) {
        ElementVisitorGraph visitor = new ElementVisitorGraph(acc);
        ElementWalker.walk(elt, visitor);
        return acc;
    }

    public static Map<Node, Var> createMapFixVarNames(Element element) {
        Collection<Var> vars = PatternVars.vars(element);
        Map<Node, Var> result = createMapFixVarNames(vars);

        return result;
    }

    public static Map<Node, Var> createMapFixVarNames(Collection<Var> vars) {
        //Set<Var> vars = NodeUtils.getVarsMentioned(nodes);
        //Set<Node> bnodes = NodeUtils.getBnodesMentioned(vars);
        Generator<Var> gen = VarGeneratorBlacklist.create("v", vars);

        Map<Node, Var> result = new HashMap<Node, Var>();
//        for(Node node : bnodes) {
//            result.put(node, gen.next());
//        }
        for(Var var : vars) {
            if(var.getName().startsWith("?") || var.getName().startsWith("/")) {
                result.put(var, gen.next());
            }
            //System.out.println(var);
        }

        return result;
    }


    public static Element fixVarNames(Element element) {
        Map<Node, Var> nodeMap = createMapFixVarNames(element);

        NodeTransform nodeTransform = NodeTransformRenameMap.create(nodeMap);
        Element result = ElementUtils.applyNodeTransform(element, nodeTransform);

        return result;
    }

    public static Element toElement(Collection<Element> elements) {
        Element result;
        if(elements.size() == 1) {
            result = elements.iterator().next();
        } else {
            ElementGroup e = new ElementGroup();
            for(Element element : elements) {
                e.addElement(element);
            }
            result = e;
        }

        return result;
    }

    public static Element unionIfNeeded(Element ... elements) {
        Element result = unionIfNeeded(Arrays.asList(elements));
        return result;
    }

    public static Element unionIfNeeded(Collection<Element> elements) {
        Element result;
        if(elements.size() == 1) {
            result = elements.iterator().next();
        } else {
            ElementUnion e = new ElementUnion();
            for(Element element : elements) {
                e.addElement(element);
            }
            result = e;
        }

        return result;
    }

    public static Element groupIfNeeded(Iterable<? extends Element> members) {
        Element result = Iterables.size(members) == 1
                ? members.iterator().next()
                : createElementGroup(members)
                ;

        return result;

    }

    public static Element groupIfNeeded(Element ... members) {
        Element result = groupIfNeeded(Arrays.asList(members));
        return result;
    }

    public static ElementGroup createElementGroup(Iterable<? extends Element> members) {
        ElementGroup result = new ElementGroup();
        for(Element member : members) {
            result.addElement(member);
        }
        return result;
    }

    public static ElementGroup createElementGroup(Element ... members) {
        ElementGroup result = new ElementGroup();
        for(Element member : members) {
            result.addElement(member);
        }
        return result;
    }

    public static Element createRenamedElement(Element element, Map<?, ? extends Node> nodeMap) {
        NodeTransform nodeTransform = NodeTransformRenameMap.create(nodeMap);
        Element result = applyNodeTransform(element, nodeTransform);
        return result;
    }

//    public static Element createRenamedElement(Element element, NodeTransform nodeTransform) {
//    	return applyNodeTransform(element, nodeTransform);
//    }

    public static Element applyNodeTransform(Element element, NodeTransform nodeTransform) {
        return applyNodeTransformBackport(element, nodeTransform);
    }

    public static Element applyNodeTransformJena(Element element, NodeTransform nodeTransform) {
        org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform elementTransform = new ElementTransformSubst2(nodeTransform);//new ElementTransformSubst2(nodeTransform);
        ExprTransform exprTransform = new ExprTransformNodeElement(nodeTransform, elementTransform);

        //Element result = ElementTransformer.transform(element, elementTransform, exprTransform);
//      Element result = org.aksw.jena_sparql_api.backports.syntaxtransform.ElementTransformer.transform(element, elementTransform, exprTransform);
        Element result = org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer.transform(element, elementTransform, exprTransform);

        return result;
    }

    // TODO As long as ApplyElementTransformVistitor cannot change the behavior to simply substitute variables -
    // - instead of doing 'SELECT (?x AS ?y)', this method is still needed...
    @Deprecated // Use TransformElementLib.transform instead
    public static Element applyNodeTransformBackport(Element element, NodeTransform nodeTransform) {
        ElementTransform elementTransform = new ElementTransformSubst2(nodeTransform);//new ElementTransformSubst2(nodeTransform);
        ExprTransform exprTransform = new ExprTransformNodeElement(nodeTransform, elementTransform);

        Element result = org.aksw.jenax.util.backport.syntaxtransform.ElementTransformer.transform(element, elementTransform, exprTransform);

        return result;
    }


    public static ElementGroup copyElements(ElementGroup target, Element source) {
        if(source instanceof ElementGroup) {
            ElementGroup es = (ElementGroup)source;

            for(Element e : es.getElements()) {
                target.addElement(e);
            }
        } else {
            target.addElement(source);
        }

        return target;
    }

    public static void mergeElements(ElementGroup target, ElementTriplesBlock etb, Element source) {
        if(source instanceof ElementTriplesBlock) {
            ElementTriplesBlock e = (ElementTriplesBlock)source;
            for(Triple t : e.getPattern()) {
                etb.addTriple(t);
            }
        } else if(source instanceof ElementGroup) {
            ElementGroup es = (ElementGroup)source;

            for(Element e : es.getElements()) {
                mergeElements(target, etb, e);
                //target.addElement(e);
            }
        } else {
            target.addElement(source);
        }
    }

    /**
     * Creates a new ElementGroup that contains the elements of the given arguments.
     * Argument ElementGroups are flattened. ElementTriplesBlocks however are not combined.
     *
     * @param first
     * @param second
     * @return
     */
    public static Element mergeElements(Element first, Element second) {
        ElementGroup tmp = new ElementGroup();
        ElementTriplesBlock etb = new ElementTriplesBlock();
        tmp.addElement(etb);


        mergeElements(tmp, etb, first);
        mergeElements(tmp, etb, second);

        // Remove empty element triple blocks
        //ElementGroup result = new ElementGroup();
        List<Element> els = new ArrayList<>();
        for(Element e : tmp.getElements()) {
            if((e instanceof ElementTriplesBlock) && ((ElementTriplesBlock)e).isEmpty()) {
                // Skip
            } else {
                els.add(e);
            }
        }

        Element result = groupIfNeeded(els);

        return result;
    }

    public static Element unionElements(Element first, Element second) {
        ElementUnion result = new ElementUnion();

        addUnionElements(result, first);
        addUnionElements(result, second);

        return result;
    }

    public static void addUnionElements(ElementUnion out, Element e) {
        if(e instanceof ElementUnion) {
            ElementUnion u = (ElementUnion)e;
            for(Element m : u.getElements()) {
                out.addElement(m);
            }
        }
        else if(e instanceof ElementGroup && ((ElementGroup)e).isEmpty()) {
            // nothing todo
        } else {
            out.addElement(e);
        }
    }

    public static List<Element> toElementList(Element element) {
        List<Element> result;

        if(element instanceof ElementGroup) {
            result = ((ElementGroup)element).getElements();
        } else {
            result = Arrays.asList(element);
        }

        // This method always returns a copy of the elements
        result = new ArrayList<Element>(result);

        return result;
    }

    /**
     * TODO This method should flatten elements recursively
     *
     * @param e
     * @return
     */
    public static Element flatten(Element e) {
        Element result;
        if(e instanceof ElementGroup) {
            ElementGroup tmp = (ElementGroup)e;
            List<Element> els = tmp.getElements();

            result = els.size() == 1 ? els.get(0) : tmp;
        } else {
            result = e;
        }

        return result;
    }

    public static ElementData createElementData(Collection<Var> vars, Collection<Binding> rows) {
        ElementData result = new ElementData();
        vars.forEach(result::add);
        rows.forEach(result::add);
        return result;
    }
}