package org.aksw.jenax.shellgebra;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.aksw.jenax.shellgebra.cmd.ArgsBuilderJena;
import org.aksw.jenax.shellgebra.cmd.ArgsBuilderRdfConvert;
import org.aksw.jenax.shellgebra.cmd.ArgsTransformRdfConvertToRapper;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;

/**
 * Test cases for the binding between
 * a java domain model for rdf conversion and
 * a concrete argument list.
 * */
public class TestRdfConvertArgBuilder {
    @Test
    public void test01() {
        List<String> expected = List.of("-i", "ntriples", "-o", "turtle", "-", "http://www.example.org/");
        ArgsBuilderRdfConvert<?> argsBuilderJena = ArgsBuilderJena.newBuilder();
        List<String> jenaArgs = argsBuilderJena.setSrcLang(Lang.NTRIPLES).setTgtFormat(RDFFormat.TURTLE).build();
        List<String> rapperArgs = ArgsTransformRdfConvertToRapper.map(jenaArgs);
        assertEquals(expected, rapperArgs);
    }
}
