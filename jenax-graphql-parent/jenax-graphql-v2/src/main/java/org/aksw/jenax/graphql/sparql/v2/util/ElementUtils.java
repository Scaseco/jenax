package org.aksw.jenax.graphql.sparql.v2.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.aksw.jenax.graphql.sparql.v2.util.backport.syntaxtransform.ElementTransformer;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ExprTransformNodeElement;

import graphql.com.google.common.collect.Iterables;

public class ElementUtils {

    public static ElementTriplesBlock createElementTriple(Node s, Node p, Node o) {
        return createElement(Triple.create(s, p, o));
    }

    public static ElementTriplesBlock createElementTriple(Node s, Node p, Node o, boolean isForward) {
        return createElement(TripleUtils.create(s, p, o, isForward));
    }

    public static ElementTriplesBlock createElement(Triple triple) {
        BasicPattern bgp = new BasicPattern();
        bgp.add(triple);
        ElementTriplesBlock result = new ElementTriplesBlock(bgp);
        return result;
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

    public static ElementGroup createElementGroup(Element ... members) {
        ElementGroup result = new ElementGroup();
        for(Element member : members) {
            result.addElement(member);
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

    public static NodeTransform createNodeTransform(Map<?, ? extends Node> nodeMap) {
        return node -> {
            Node r = nodeMap.get(node);
            if (r == null) {
                r = node;
            } return r;
        };
    }

    public static Element createRenamedElement(Element element, Map<?, ? extends Node> nodeMap) {
        NodeTransform nodeTransform = createNodeTransform(nodeMap);
        Element result = applyNodeTransform(element, nodeTransform);
        return result;
    }

//    public static Element createRenamedElement(Element element, NodeTransform nodeTransform) {
//    	return applyNodeTransform(element, nodeTransform);
//    }

    public static Element applyNodeTransform(Element element, NodeTransform nodeTransform) {
        // return applyNodeTransformJena(element, nodeTransform);
        return applyNodeTransformBackport(element, nodeTransform);
    }

    public static Element applyNodeTransformBackport(Element element, NodeTransform nodeTransform) {
        ElementTransform elementTransform = new ElementTransformSubst2(nodeTransform);//new ElementTransformSubst2(nodeTransform);

        // Need to use backport version because of substitution in aggregators
        ExprTransform exprTransform = new org.aksw.jenax.graphql.sparql.v2.util.backport.syntaxtransform.ExprTransformNodeElement(nodeTransform, elementTransform);

        Element result = ElementTransformer.transform(element, elementTransform, exprTransform);

        return result;
    }

    public static Element applyNodeTransformJena(Element element, NodeTransform nodeTransform) {
        org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform elementTransform = new ElementTransformSubst2(nodeTransform);//new ElementTransformSubst2(nodeTransform);
        ExprTransform exprTransform = new ExprTransformNodeElement(nodeTransform, elementTransform);

        //Element result = ElementTransformer.transform(element, elementTransform, exprTransform);
//      Element result = org.aksw.jena_sparql_api.backports.syntaxtransform.ElementTransformer.transform(element, elementTransform, exprTransform);
        Element result = org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer.transform(element, elementTransform, exprTransform);

        return result;
    }
}
