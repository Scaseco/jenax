package org.aksw.jenax.io.json.accumulator;

import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.aksw.jenax.io.rdf.json.RdfArray;
import org.aksw.jenax.io.rdf.json.RdfElementVisitorRdfToJson;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Quad;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;



public class EdgeBasedAccumulator {
    public static void main(String[] args) throws Exception {
        AggJsonObject movieObject = new AggJsonObject();

        /*
         * movie: {
         *   actor: {
         *     label
         *   },
         *   label
         * }
         */

        AggJsonProperty actorEdge = AggJsonProperty.of(NodeFactory.createLiteral("actor"), NodeFactory.createURI("urn:actor"), true);
        movieObject.addPropertyAggregator(actorEdge);

        AggJsonObject actorObject = new AggJsonObject();
        actorEdge.setTargetAgg(actorObject);

        AggJsonProperty actorLabelEdge = AggJsonProperty.of(NodeFactory.createLiteral("label"), NodeFactory.createURI("urn:actorLabel"), true);
        actorLabelEdge.setSingle(true);
        AggJsonLiteral actorLabelValue = new AggJsonLiteral();
        actorLabelEdge.setTargetAgg(actorLabelValue);
        actorObject.addPropertyAggregator(actorLabelEdge);

        AggJsonProperty moveLabelEdge = AggJsonProperty.of(NodeFactory.createLiteral("label"), NodeFactory.createURI("urn:movieLabel"), true);
        movieObject.addPropertyAggregator(moveLabelEdge);

        AggJsonLiteral movieLabelValue = new AggJsonLiteral();

        // JsonTreeWriter x = new JsonTreeWriter();
        // x.get();
        moveLabelEdge.setTargetAgg(movieLabelValue);

        List<Quad> data = Arrays.asList(
            // movie0: label only for movie
            create("urn:movie0", "urn:movie0", null, null),
            create("urn:movie0", "urn:movie0", "urn:movieLabel", "urn:movie[0].label[0]"),

            // movie1: labels for actors and movies
            create("urn:movie1", "urn:movie1", null, null),
            create("urn:movie1", "urn:movie1", "urn:actor", "urn:actor1"),
            create("urn:movie1", "urn:actor1", "urn:actorLabel", "urn:actor[1].label[0]"),
            create("urn:movie1", "urn:actor1", "urn:actorLabel", "urn:actor[1].label[1]"),
            create("urn:movie1", "urn:movie1", "urn:movieLabel", "urn:movie[1].label[0]"),
            create("urn:movie1", "urn:movie1", "urn:movieLabel", "urn:movie[1].label[1]"),
//
//            // Movie0: label only for actor
            create("urn:movie2", "urn:movie2", null, null),
            create("urn:movie2", "urn:movie2", "urn:actor", "urn:actor2"),
            create("urn:movie2", "urn:actor2", "urn:actorLabel", "urn:actor[2].label[0]")


            // Artifical errors
//          create("urn:movie1", "urn:actor1", "urn:movieLabel", "urn:movie[1].label[0]"),
//          create("urn:movie1", "urn:actor1", "urn:movieLabel", "urn:movie[1].label[1]"),


//            create("urn:movie1", "urn:actor1", "urn:actorLabel", "urn:actor[1].label[1]"),
//            create("urn:movie2", "urn:movie2", "urn:actor", "urn:actor2"),
//            create("urn:movie2", "urn:movie2", "urn:movieLabel", "urn:movie[2].label[0]")
        );

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonWriter writer = gson.newJsonWriter(new OutputStreamWriter(System.out));

        // gson.fromJson(/null, null)
        AccContext accContext = new AccContext(new RdfObjectNotationWriterViaJson(gson, writer), true, true);
        accContext.setErrorHandler(ev -> {
            System.err.println("Error: " + ev);
        });

        // accContext.serialize = true;
        accContext.materialize = true;

        // JsonArray materialized = new JsonArray();
        RdfArray materialized = new RdfArray();
        try {
            writer.beginArray();
            AccJsonDriver driver = AccJsonDriver.of(movieObject.newAccumulator(), false);
            driver.asStream(accContext, data.stream()).map(Entry::getValue).forEach(materialized::add);
            writer.endArray();
        } finally {
            writer.flush();
        }
        // writer.close(); // Don't close system.out

        System.out.println();

        JsonElement trueJson = materialized.accept(new RdfElementVisitorRdfToJson());

        System.out.println("Materialized: " + gson.toJson(trueJson));
    }

    public static Quad create(String g, String s, String p, String o) {
        return Quad.create(NodeFactory.createURI(g), NodeFactory.createURI(s), p == null ? Node.ANY : NodeFactory.createURI(p), o == null ? Node.ANY : NodeFactory.createURI(o));
    }
}



