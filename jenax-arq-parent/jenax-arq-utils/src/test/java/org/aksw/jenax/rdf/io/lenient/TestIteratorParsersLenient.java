package org.aksw.jenax.rdf.io.lenient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.system.AsyncParser;
import org.junit.Assert;
import org.junit.Test;

public class TestIteratorParsersLenient {

    @Test
    public void testLenientParser() throws IOException {
        String str = """
            <urn:s> <urn:p> <urn:o> . GARBAGE

            here be dragons .


            <urn:this> <urn:is> <urn:valid> .

            <urn:run> <urn:away> "literal
            more dragons.

            <urn:valid> <urn:once> "more" .

            # comment

            error

            """;

        String expectedStr = """
            <urn:s> <urn:p> <urn:o> .
            <urn:this> <urn:is> <urn:valid> .
            <urn:valid> <urn:once> "more" .
        """;
        List<Triple> expected;
        try (Stream<Triple> stream = AsyncParser.of(RDFParserBuilder.create().fromString(expectedStr).lang(Lang.NTRIPLES)).streamTriples()) {
            expected = stream.toList();
        }

        List<Triple> actual;
        try (InputStream in = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8))) {
            actual = Iter.toList(IteratorParsersLenient.createIteratorNTriples(in));
        }

        Assert.assertEquals(expected, actual);
    }
}
