package org.aksw.jenax.shellgebra.cmd;

import java.util.Optional;

import org.aksw.shellgebra.registry.content.Tool;

public class ContentConvertProviderOverRapper
    implements ContentConvertProvider
{
    @Override
    public Optional<Tool> getConverter(OpSpecContentConvertRdf spec) {
        return getConverter(spec.sourceFormat(), spec.targetFormat(), spec.baseIri());
    }

    public Optional<Tool> getConverter(String srcLangStr, String tgtFormatStr, String base) {
        Tool tool = null;
        try {
            ArgsBuilderRapper tmp = new ArgsBuilderRapper();
            tmp = tmp.setSrcLang(srcLangStr).setTgtFormat(tgtFormatStr).setBaseUri(base);
            tool = new Tool("rapper", tmp);
        } catch (IllegalArgumentException e) {
            // not supported
        }

        return Optional.ofNullable(tool);
    }
}

/*
Main options:
      -i FORMAT, --input FORMAT   Set the input format/parser to one of:
        rdfxml          RDF/XML (default)
        ntriples        N-Triples
        turtle          Turtle Terse RDF Triple Language
        trig            TriG - Turtle with Named Graphs
        rss-tag-soup    RSS Tag Soup
        grddl           Gleaning Resource Descriptions from Dialects of Languages
        guess           Pick the parser to use using content type and URI
        rdfa            RDF/A via librdfa
        json            RDF/JSON (either Triples or Resource-Centric)
        nquads          N-Quads
      -I URI, --input-uri URI     Set the input/parser base URI. '-' for none.
                                  Default is INPUT-BASE-URI argument value.

      -o FORMAT, --output FORMAT  Set the output format/serializer to one of:
        ntriples        N-Triples (default)
        turtle          Turtle Terse RDF Triple Language
        mkr             mKR my Knowledge Representation Language
        rdfxml-xmp      RDF/XML (XMP Profile)
        rdfxml-abbrev   RDF/XML (Abbreviated)
        rdfxml          RDF/XML
        rss-1.0         RSS 1.0
        atom            Atom 1.0
        dot             GraphViz DOT format
        json-triples    RDF/JSON Triples
        json            RDF/JSON Resource-Centric
        html            HTML Table
        nquads          N-Quads
*/
