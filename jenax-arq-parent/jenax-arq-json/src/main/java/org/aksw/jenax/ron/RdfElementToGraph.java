package org.aksw.jenax.ron;

import java.util.stream.Stream;

import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.path.P_Path0;

import com.google.common.collect.Streams;

public class RdfElementToGraph {

    public static Graph toGraph(RdfElement elt) {
        Graph result = GraphFactory.createDefaultGraph();
        toGraph(result, elt);
        return result;
    }

    public static Graph toGraph(Graph dest, RdfElement elt) {
        streamEffectiveTriples(elt).forEach(dest::add);
        return dest;
    }

    public static Model toModel(RdfElement elt) {
        Model result = ModelFactory.createDefaultModel();
        toGraph(result.getGraph(), elt);
        return result;
    }

    public static Model toModel(Model dest, RdfElement elt) {
        Graph g = dest.getGraph();
        streamEffectiveTriples(elt).forEach(g::add);
        return dest;
    }

    public static Stream<Triple> streamEffectiveTriples(RdfElement element) {
        Stream<Triple> result;
        if (element.isObject()) {
            result = streamEffectiveTriples_Object(element.getAsObject());
        } else if (element.isArray()) {
            result = streamEffectiveTriples_Array(element.getAsArray());
        } else {
            result = Stream.empty();
        }
        return result;
    }

    public static Stream<Triple> streamEffectiveTriples_Array(RdfArray element) {
        return Streams.stream(element).flatMap(RdfElementToGraph::streamEffectiveTriples);
    }

    public static Stream<Triple> streamEffectiveTriples_Object(RdfObject element) {
        Node s = element.getInternalId();
        return element.getMembers().entrySet().stream().flatMap(member -> {
            P_Path0 p = member.getKey();
            RdfElement o = member.getValue();
            Stream<Triple> r;
            if (o instanceof RdfElementNodeBase nb) {
                Triple t = TripleUtils.create(s, p, nb.getInternalId());
                Stream<Triple> subStream = streamEffectiveTriples(o);
                r = Stream.concat(Stream.of(t), subStream);
            } else {
                r = Stream.empty();
            }
            return r;
        });
    }
}
