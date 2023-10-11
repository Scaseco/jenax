package org.aksw.jenax.io.json.accumulator;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Quad;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.internal.bind.JsonTreeWriter;
import com.google.gson.stream.JsonWriter;



public class EdgeBasedAccumulator {
    public static void main(String[] args) throws Exception {
        AccJsonNodeObject movieObject = new AccJsonNodeObject();

        /*
         * movie: {
         *   actor: {
         *     label
         *   },
         *   label
         * }
         */

        AccJsonEdge actorEdge = new AccJsonEdgeImpl("actor", "urn:actor", true);
        movieObject.addEdge(actorEdge);

        AccJsonNodeObject actorObject = new AccJsonNodeObject();
        actorEdge.setTargetAcc(actorObject);

        AccJsonEdge actorLabelEdge = new AccJsonEdgeImpl("label", "urn:actorLabel", true);
        actorLabelEdge.setSingle(true);
        AccJsonNodeLiteral actorLabelValue = new AccJsonNodeLiteral();
        actorLabelEdge.setTargetAcc(actorLabelValue);
        actorObject.addEdge(actorLabelEdge);

        AccJsonEdge moveLabelEdge = new AccJsonEdgeImpl("label", "urn:movieLabel", true);
        movieObject.addEdge(moveLabelEdge);

        AccJsonNodeLiteral movieLabelValue = new AccJsonNodeLiteral();

        // JsonTreeWriter x = new JsonTreeWriter();
        // x.get();
        moveLabelEdge.setTargetAcc(movieLabelValue);

        List<Quad> data = Arrays.asList(
            // movie0: label only for movie
            create("urn:movie0", "urn:movie0", "urn:movieLabel", "urn:movie[0].label[0]"),

            // movie1: labels for actors and movies
            create("urn:movie1", "urn:movie1", "urn:actor", "urn:actor1"),
            create("urn:movie1", "urn:actor1", "urn:actorLabel", "urn:actor[1].label[0]"),
            create("urn:movie1", "urn:actor1", "urn:actorLabel", "urn:actor[1].label[1]"),
            create("urn:movie1", "urn:movie1", "urn:movieLabel", "urn:movie[1].label[0]"),
            create("urn:movie1", "urn:movie1", "urn:movieLabel", "urn:movie[1].label[1]"),

            // Movie0: label only for actor
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
        AccContext accContext = new AccContext(gson, writer, true, true);
        accContext.setErrorHandler(ev -> {
            System.err.println("Error: " + ev);
        });

        // accContext.serialize = true;
        accContext.materialize = true;

        JsonArray materialized = new JsonArray();
        try {
            writer.beginArray();
            AccNodeDriver driver = new AccNodeDriver(movieObject);
            driver.asStream(accContext, data.stream()).map(Entry::getValue).forEach(materialized::add);
            writer.endArray();
        } finally {
            writer.flush();
        }
        // writer.close(); // Don't close system.out

        System.out.println();
        System.out.println("Materialized: " + gson.toJson(materialized));
    }

    public static Quad create(String g, String s, String p, String o) {
        return Quad.create(NodeFactory.createURI(g), NodeFactory.createURI(s), NodeFactory.createURI(p), NodeFactory.createURI(o));
    }
}



