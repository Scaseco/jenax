package org.aksw.jenax.arq.util.io;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.graph.GraphFactory;

/** Mixin for RDF data generators that can produce graphs. */
public interface ToGraph {
    Graph toGraph(Graph graph);

    default Graph toGraph() {
        return toGraph(GraphFactory.createDefaultGraph());
    }

    default Model toModel() {
        return ModelFactory.createModelForGraph(toGraph());
    }

    default Model toModel(Model model) {
        toGraph(model.getGraph());
        return model;
    }
}
