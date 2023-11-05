package org.aksw.jenax.model.shacl.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.aksw.jenax.model.shacl.domain.ShPrefixDeclaration;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.attributes.methodaccess.NoOpMethodAccessValidator;
import io.pebbletemplates.pebble.loader.StringLoader;
import io.pebbletemplates.pebble.template.PebbleTemplate;

public class ShPebbleUtils {

    static { JenaSystem.init(); }

    public static void main(String[] args) {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource()
                .addProperty(RDF.type, m.createResource("Foo"))
                .addProperty(RDF.type, m.createResource("Bar"))
                .addProperty(RDF.type, m.createResource("Baz"))
                .addProperty(RDFS.label, "bar")
                .as(ShPrefixDeclaration.class);

        System.out.println(createRenderer("<h1>Greetings {{this.getProperty(RDFS.label).getString()}}</h1>").apply(Map.of("this", r)));
        System.out.println(createRenderer("<h1>Hello {{this.name}}</h1>").apply(Map.of("this", r)));
        System.out.println(createRenderer("<ul> {% for stmt in this.listProperties().toList() %} <li>item</li> {% endfor %} </ul>").apply(Map.of("this", r)));
        System.out.println(createRenderer("<ul> {% set items = this.listProperties().toSet() %} items: {{items}} {% for stmt in items %} <li>item</li> {% endfor %} </ul>").apply(Map.of("this", r)));
        System.out.println(forRdfNode("<table>\n"
                + "{% for p in RDF.properties(this) %}\n"
                + "  {% for o in RDF.objects(this, p) %}\n"
                + "     {% set oStr = RDF.strContains(o.getURI(), 'Baz') %}"
                + "      oStr: {{oStr}}"
                + "    <tr>\n"
                + "      <td> {% if loop.isFirst %} {{p}} {% endif %} </td>\n"
                + "      <td> {{RDF.toHtml(o) | raw}} </td>\n"
                + "    </tr>\n"
                + "  {% endfor %}\n"
                + "{% endfor %}\n"
                + "</table>\n"
                + "").apply(r));

        // r.listProperties().toList().forEach(x -> x.getPredicate())
        // com.google.common.collect.Sets.newLinkedHashSet();
    }

    /** Create a template for an RDF node which is bound to the variable 'this' */
    public static Function<RDFNode, String> forRdfNode(String templateString) {
        Function<Map<String, Object>, String> renderer = createRenderer(templateString);
        return rdfNode -> {
            Map<String, Object> cxt = new HashMap<>();
            cxt.put("this", rdfNode);
            cxt.put("RDF", new RDFUtils());
            String r = renderer.apply(cxt);
            return r;
        };
    }

    public static Function<Map<String, Object>, String> createRenderer(String templateString) {
        StringLoader loader = new StringLoader();
        PebbleEngine engine = new PebbleEngine.Builder()
                .methodAccessValidator(new NoOpMethodAccessValidator())
                .loader(loader)
                .build();
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
