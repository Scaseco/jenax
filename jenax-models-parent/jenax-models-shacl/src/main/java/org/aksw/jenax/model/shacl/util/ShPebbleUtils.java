package org.aksw.jenax.model.shacl.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.function.Function;

import org.aksw.jenax.model.shacl.domain.ShPrefixDeclaration;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDFS;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.StringLoader;
import io.pebbletemplates.pebble.template.PebbleTemplate;

public class ShPebbleUtils {

    static { JenaSystem.init(); }

    public static void main(String[] args) {
        Resource r = ModelFactory.createDefaultModel().createResource()
                .addProperty(RDFS.label, "Hauaeou")
                .as(ShPrefixDeclaration.class);

        System.out.println(createRenderer("<h1>Greetings {{this.getProperty(RDFS.label).getString()}}</h1>").apply(Map.of("this", r)));
        System.out.println(createRenderer("<h1>Hello {{this.name}}</h1>").apply(Map.of("this", r)));
    }

    /** Create a template for an RDF node which is bound to the variable 'this' */
    public static Function<RDFNode, String> forRdfNode(String templateString) {
        Function<Map<String, Object>, String> renderer = createRenderer(templateString);
        return rdfNode -> {
            Map<String, Object> cxt = Map.of("this", rdfNode);
            String r = renderer.apply(cxt);
            return r;
        };
    }

    public static Function<Map<String, Object>, String> createRenderer(String templateString) {
        StringLoader loader = new StringLoader();
        PebbleEngine engine = new PebbleEngine.Builder().loader(loader).build();
        PebbleTemplate compiledTemplate = engine.getTemplate(templateString);
        return cxt -> {
            String r;
            try (Writer writer = new StringWriter()) {
                compiledTemplate.evaluate(writer, cxt);
                r = writer.toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return r;
        };
    }
}
