package org.aksw.jenax.model.shacl.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * A mapping from
 */
public class ShTemplateRegistry {
    protected Map<Node, Function<RDFNode, ?>> viewToTemplate = new LinkedHashMap<>();
    protected Multimap<Node, Node> shapeToViews = HashMultimap.create();

    public Map<Node, Function<RDFNode, ?>> getViewToTemplate() {
        return viewToTemplate;
    }

    public Multimap<Node, Node> getShapeToViews() {
        return shapeToViews;
    }

    public void clear() {
        viewToTemplate.clear();
        shapeToViews.clear();
    }

    public ShTemplateRegistry addAll(ShTemplateRegistry other) {
        viewToTemplate.putAll(other.getViewToTemplate());
        shapeToViews.putAll(other.getShapeToViews());
        return this;
    }
}
