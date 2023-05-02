package org.aksw.jenax.arq.datatype;

import java.util.Arrays;

import org.aksw.jenax.arq.util.node.NodeList;
import org.aksw.jenax.arq.util.node.NodeListImpl;
import org.aksw.jenax.arq.util.node.NodeSet;
import org.aksw.jenax.arq.util.node.NodeSetImpl;
import org.apache.jena.ext.com.google.common.collect.ImmutableSet;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Test;

public class TestDatatypes {
    @Test
    public void test() {
        String NS = "http://www.example.org/";
        Property hasQuery = ResourceFactory.createProperty(NS + "hasQuery");
        Property hasExpr = ResourceFactory.createProperty(NS + "hasExpr");
        Property hasBinding = ResourceFactory.createProperty(NS + "hasBinding");
        Property hasArray = ResourceFactory.createProperty(NS + "hasArray");
        Property hasSet = ResourceFactory.createProperty(NS + "hasSet");

        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("eg", NS);
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("xdt", "http://jsa.aksw.org/dt/sparql/");

        Query query = QueryFactory.create("SELECT * { ?s ?p ?o }");
        Expr expr = ExprUtils.parse("CONCAT('foo', ?bar)");
        Binding binding = BindingFactory.binding(Var.alloc("s"), RDF.type.asNode());
        NodeList array = new NodeListImpl(Arrays.asList(RDFS.Class.asNode(), OWL.Class.asNode()));
        NodeSet set = new NodeSetImpl(ImmutableSet.of(RDFS.comment.asNode(), RDFS.label.asNode()));

        String s = "urn:example:s";

        // Note: model.createTypedLiteral is needed below in order to interpret specific sub classes
        // such as Binding1 and E_StrConcat against the datatype's "base" classes Binding and Expr, respectively.
        model.createResource(s)
                .addLiteral(hasQuery, query)
                .addProperty(hasExpr, model.createTypedLiteral(expr, RDFDatatypeExpr.get()))
                .addProperty(hasBinding, model.createTypedLiteral(binding, RDFDatatypeBinding.get()))
                .addLiteral(hasArray, model.createTypedLiteral(array, RDFDatatypeNodeList.get()))
                .addLiteral(hasSet, model.createTypedLiteral(set, RDFDatatypeNodeSet.get()))
                ;

        // Roundtrip: First serialize the model to string, then deserialize the string and finally
        // assert equality of the deserialized java objects with the original ones
        String modelStr = RDFWriter.create().source(model).format(RDFFormat.TURTLE_PRETTY).asString();
        // System.out.println(modelStr);
        Resource x = RDFParser.fromString(modelStr).lang(Lang.TURTLE).toModel().createResource(s);

        Assert.assertEquals(query, x.getProperty(hasQuery).getObject().asLiteral().getValue());
        Assert.assertEquals(expr, x.getProperty(hasExpr).getObject().asLiteral().getValue());
        Assert.assertEquals(binding, x.getProperty(hasBinding).getObject().asLiteral().getValue());
        Assert.assertEquals(array, x.getProperty(hasArray).getObject().asLiteral().getValue());
        Assert.assertEquals(set, x.getProperty(hasSet).getObject().asLiteral().getValue());
    }
}
