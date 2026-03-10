package org.aksw.jenax.shellgebra.cmd;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aksw.jenax.arq.util.io.RDFConverterMetaData;
import org.aksw.jenax.arq.util.lang.RDFLanguagesEx;
import org.aksw.shellgebra.shim.core.ArgsBuilder;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;

/** Convert parameters of OpStreamContentConvert to arguments for a command invocation.*/
public class ArgsBuilderRapper
    extends ArgsBuilderRdfConvertBase<ArgsBuilderRapper>
{
    protected Map<Lang, String> inputLangMap = initInputLangMap(new LinkedHashMap<>());
    protected Map<Lang, String> outLangMap = initOutputLangMap(new LinkedHashMap<>());

    public static ArgsBuilderRapper newBuilder() {
        return new ArgsBuilderRapper();
    }

    public static Map<Lang, String> initInputLangMap(Map<Lang, String> inLangMap) {
        inLangMap.put(Lang.RDFXML, "rdfxml");
        inLangMap.put(Lang.NTRIPLES, "ntriples");
        inLangMap.put(Lang.TURTLE, "turtle");
        inLangMap.put(Lang.TRIG, "trig");
        inLangMap.put(Lang.NQUADS, "nquads");
        return inLangMap;
    }

        /*
        ntriples        N-Triples (default)
        turtle          Turtle Terse RDF Triple Language
        nquads          N-Quads
        rdfxml          RDF/XML
        ---
        rdfxml-xmp      RDF/XML (XMP Profile)
        rdfxml-abbrev   RDF/XML (Abbreviated)
        mkr             mKR my Knowledge Representation Language
        rss-1.0         RSS 1.0
        atom            Atom 1.0
        dot             GraphViz DOT format
        json-triples    RDF/JSON Triples
        json            RDF/JSON Resource-Centric
        html            HTML Table
        */
    public static Map<Lang, String> initOutputLangMap(Map<Lang, String> outLangMap) {
        outLangMap.put(Lang.NTRIPLES, "ntriples");
        outLangMap.put(Lang.TURTLE, "turtle");
        outLangMap.put(Lang.NQUADS, "nquads");
        outLangMap.put(Lang.RDFXML, "rdfxml");
        return outLangMap;
    }

    public ArgsBuilderRapper() {
        super();
    }

    public static RDFConverterMetaData getDefaultMetaData() {
        return new ArgsBuilderRapper().getMetaData();
    }

    private final RDFConverterMetaData metaData = new RDFConverterMetaData() {
        @Override
        public Collection<Lang> getSupportedInputLangs() {
            return inputLangMap.keySet();
        }

        @Override
        public Collection<RDFFormat> getSupportedOutputFormats() {
            return outLangMap.keySet().stream().map(lang -> new RDFFormat(lang, RDFFormat.PLAIN)).toList();
        }

        @Override
        public RDFFormat getDefaultOutputFormat(Lang outLang) {
            return new RDFFormat(outLang, RDFFormat.PLAIN);
        }
    };

    // @Override
    public RDFConverterMetaData getMetaData() {
        return metaData;
    }

    @Override
    protected String processSrcLang(String srcLangStr) {
        Lang srcLang = RDFLanguagesEx.findLang(srcLangStr);
        if (srcLang == null) {
            throw new IllegalArgumentException("Could not resolve argument to a jena lang: " + srcLangStr);
        }

        String tmpSrcArg = inputLangMap.get(srcLang);
        if (tmpSrcArg == null) {
            throw new IllegalArgumentException("Could not resolve argument to a rapper input format value: " + srcLangStr);
        }
        return tmpSrcArg;
    }

    @Override
    protected String processTgtFormat(String tgtFormatStr) {
        RDFFormat tgtFormat = RDFLanguagesEx.findRdfFormat(tgtFormatStr);
        if (tgtFormat == null) {
            throw new IllegalArgumentException("Could not resolve argument to a jena rdf format: " + tgtFormatStr);
        }

        Lang tgtLang = tgtFormat.getLang();
        String tmpTgtArg = inputLangMap.get(tgtLang);
        if (tmpTgtArg == null) {
            throw new IllegalArgumentException("Could not resolve argument to a rapper input format value: " + tgtFormatStr);
        }
        return tmpTgtArg;
    }

    @Override
    public ArgsBuilderRapper setBaseUri(String baseUri) {
        this.baseUri = baseUri;
        return this;
    }

    @Override
    public List<String> build() {
        String base = baseUri == null ? "http://www.example.org/" : baseUri;
        List<String> result = ArgsBuilder.newBuilder()
            .opt("-i", srcFmtArg)
            .opt("-o", tgtFmtArg)
            .arg("-") // XXX support direct file input?
            .arg(base)
            .build();
        return result;
    }
}
