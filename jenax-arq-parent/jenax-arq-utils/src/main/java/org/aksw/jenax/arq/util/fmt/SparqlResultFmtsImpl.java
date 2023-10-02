package org.aksw.jenax.arq.util.fmt;

import org.aksw.jenax.arq.util.lang.RDFLanguagesEx;
import org.apache.jena.atlas.web.AcceptList;
import org.apache.jena.atlas.web.MediaRange;
import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.riot.*;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.system.StreamRDFWriter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class for providing (default) mappings for the result types defined by SPARQL
 * to Lang and RDFFormat instances
 *
 * Use {@link SparqlQueryFmts} for controlling formats on the query type such that e.g.
 * describe and construct queries map to different formats.
 */
public class SparqlResultFmtsImpl implements SparqlResultFmts {
    public static final SparqlResultFmts DEFAULT = createDefault();
    public static final SparqlResultFmts XML = createXml();
    public static final SparqlResultFmts JSON = createJson();
    public static final SparqlResultFmts TXT = createTxt();

    protected Lang askResult;
    protected Lang bindings;
    protected RDFFormat triples;
    protected RDFFormat quads;
    protected Lang unknown;

    private static List<SparqlResultFmts> sisterFormats = new ArrayList<>();
    static {
        //sisterFormats.add(new SparqlResultFmtsImpl(unknown, askResult, bindings, triples, quads));
        sisterFormats.add(new SparqlResultFmtsImpl(ResultSetLang.RS_XML, ResultSetLang.RS_XML, ResultSetLang.RS_XML, RDFFormat.RDFXML_PLAIN, RDFFormat.TRIX));
        sisterFormats.add(new SparqlResultFmtsImpl(ResultSetLang.RS_JSON, ResultSetLang.RS_JSON, ResultSetLang.RS_JSON, RDFFormat.JSONLD, RDFFormat.JSONLD));
        sisterFormats.add(new SparqlResultFmtsImpl(ResultSetLang.RS_JSON, ResultSetLang.RS_JSON, ResultSetLang.RS_JSON, RDFFormat.RDFJSON, RDFFormat.JSONLD));
        sisterFormats.add(new SparqlResultFmtsImpl(ResultSetLang.RS_JSON, ResultSetLang.RS_JSON, ResultSetLang.RS_JSON, RDFFormat.TURTLE_BLOCKS, RDFFormat.TRIG_BLOCKS));
        sisterFormats.add(new SparqlResultFmtsImpl(ResultSetLang.RS_TSV, ResultSetLang.RS_TSV, ResultSetLang.RS_TSV, RDFFormat.NT, RDFFormat.NQ));
        sisterFormats.add(new SparqlResultFmtsImpl(ResultSetLang.RS_CSV, ResultSetLang.RS_CSV, ResultSetLang.RS_CSV, RDFFormat.NT, RDFFormat.NQ));
        sisterFormats.add(new SparqlResultFmtsImpl(ResultSetLang.RS_Text, ResultSetLang.RS_Text, ResultSetLang.RS_Text, RDFFormat.TURTLE_BLOCKS, RDFFormat.TRIG_BLOCKS));
    }

    public SparqlResultFmtsImpl(Lang unknown, Lang askResult, Lang bindings, RDFFormat triples, RDFFormat quads) {
        super();
        this.unknown = unknown;
        this.askResult = askResult;
        this.bindings = bindings;
        this.triples = triples;
        this.quads = quads;
    }

    public static SparqlResultFmts createDefault() {
        return new SparqlResultFmtsImpl(Lang.JSONLD, ResultSetLang.RS_JSON,
                ResultSetLang.RS_JSON, RDFFormat.TURTLE_BLOCKS, RDFFormat.TRIG_BLOCKS);
    }

    public static SparqlResultFmts createJson() {
        return new SparqlResultFmtsImpl(Lang.JSONLD11, ResultSetLang.RS_JSON,
                ResultSetLang.RS_JSON, RDFFormat.JSONLD11, RDFFormat.JSONLD11);
    }

    public static SparqlResultFmts createXml() {
        return new SparqlResultFmtsImpl(Lang.RDFXML, ResultSetLang.RS_XML,
                ResultSetLang.RS_XML, RDFFormat.RDFXML, RDFFormat.TRIX);
    }

    public static SparqlResultFmts createTxt() {
        return new SparqlResultFmtsImpl(Lang.TRIG, ResultSetLang.RS_Text,
                ResultSetLang.RS_Text, RDFFormat.TURTLE_BLOCKS, RDFFormat.TRIG_BLOCKS);
    }

    public static SparqlResultFmts createCsv() {
        return new SparqlResultFmtsImpl(Lang.NQ, ResultSetLang.RS_CSV,
                ResultSetLang.RS_CSV, RDFFormat.NT, RDFFormat.NQ);
    }

    public static SparqlResultFmts createTsv() {
        return new SparqlResultFmtsImpl(Lang.NQ, ResultSetLang.RS_TSV,
                ResultSetLang.RS_TSV, RDFFormat.NT, RDFFormat.NQ);
    }

    protected static void setSisterDefaults(SparqlResultFmtsImpl impl, SparqlResultType sourceFmt,
                                            EnumSet<SparqlResultType> targetFmts) {
        for (SparqlResultFmts f: sisterFormats) {
            if (Objects.equals(f.get(sourceFmt), impl.get(sourceFmt))) {
                for (SparqlResultType s: targetFmts) {
                    impl.set(s, f.get(s));
                }
            }
        }
    }

    protected void set(SparqlResultType s, Object o) {
        switch (s) {
            case AskResult:
                this.askResult = (Lang) o;
                return;
            case Bindings:
                this.bindings = (Lang) o;
                return;
            case Quads:
                this.quads = (RDFFormat) o;
                return;
            case Triples:
                this.triples = (RDFFormat) o;
                return;
            case Unknown:
                this.unknown = (Lang) o;
                return;
        }
    }

    /** Compute the SparqlResultFmts for a given http accept header. */
    public static SparqlResultFmts forContentTypes(AcceptList acceptableContentTypes) {
        // the purpose of this loop is to "inject" the raw sparql-result-set content types as fake Accept headers
        // when the regular generic CTs are requested (e.g. application/xml)
        List<MediaRange> mrList = new ArrayList<>(acceptableContentTypes.entries());
        acceptableContentTypes.entries().forEach(e -> {
            String origContentTypeStr = e.getContentTypeStr();
            Lang lang = WebContent.contentTypeToLangResultSet(origContentTypeStr);
            if (lang != null) {
                String contentTypeStr = lang.getContentType().getContentTypeStr();
                if (acceptableContentTypes.entries().stream().noneMatch(f -> f.getContentTypeStr().equals(contentTypeStr))) {
                    MediaRange mr = new MediaRange(e.toHeaderString().replace(origContentTypeStr, contentTypeStr));
                    mrList.add(mr);
                }
            }
        });
        AcceptList acceptList = new AcceptList(mrList);

        SparqlResultFmtsImpl r = (SparqlResultFmtsImpl) SparqlResultFmtsImpl.createDefault();
        List<Lang> acceptableRdfLangs = acceptableContentTypes.entries().stream()
                .map(MediaType::getContentTypeStr)
                .map(RDFLanguages::contentTypeToLang)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<Lang> resultSetFormats = new ArrayList<>();
        resultSetFormats.addAll(RDFLanguagesEx.getResultSetFormats());
        // the text/plain result set is not in RDFLanguages
        resultSetFormats.add(ResultSetLang.RS_Text);

        List<Lang> quadLangs = RDFLanguagesEx.getQuadLangs();
        List<Lang> tripleLangs = RDFLanguagesEx.getTripleLangs();

        r.bindings = RDFLanguagesEx.findLangMatchingAcceptList(acceptList, resultSetFormats, r.bindings);
        setSisterDefaults(r, SparqlResultType.Bindings, EnumSet.of(SparqlResultType.Unknown, SparqlResultType.AskResult, SparqlResultType.Triples, SparqlResultType.Quads));
        r.askResult = RDFLanguagesEx.findLangMatchingAcceptList(acceptList, resultSetFormats, r.askResult);
        r.unknown = acceptableRdfLangs.stream().findFirst().orElse(r.unknown);

        Collection<RDFFormat> streamFormats = StreamRDFWriter.registered();
        Collection<RDFFormat> quadFormats = RDFWriterRegistry.registeredDatasetFormats();
        Collection<RDFFormat> tripleFormats = RDFWriterRegistry.registeredGraphFormats();

        boolean quadsSet = false;
        boolean triplesSet = false;

        search:
        for (MediaType mt: acceptableContentTypes.entries()) {
            Lang lang = RDFLanguages.contentTypeToLang(mt.getContentTypeStr());
            if (lang == null)
                continue search;

            String variant = mt.getParameter("variant");
            {
                RDFFormat streamDefaultFormat = StreamRDFWriter.defaultSerialization(lang);
                RDFFormatVariant streamDefaultFormatVariant = streamDefaultFormat != null ?
                        streamDefaultFormat.getVariant() : null;
                if (streamDefaultFormat != null
                        && (variant == null
                        || (streamDefaultFormatVariant != null
                        && variant.equalsIgnoreCase(streamDefaultFormatVariant.toString())))) {
                    // we're done for this
                    if (!quadsSet && quadLangs.contains(lang)) {
                        r.quads = streamDefaultFormat;
                        quadsSet = true;
                        if (triplesSet)
                            break search;
                        else setSisterDefaults(r, SparqlResultType.Quads, EnumSet.of(SparqlResultType.Triples));
                    }
                    if (!triplesSet && tripleLangs.contains(lang)) {
                        r.triples = streamDefaultFormat;
                        triplesSet = true;
                        if (quadsSet)
                            break search;
                        else setSisterDefaults(r, SparqlResultType.Triples, EnumSet.of(SparqlResultType.Quads));
                    }
                }
            }

            for (RDFFormat f: streamFormats) {
                RDFFormatVariant formatVariant = f.getVariant();
                if (f.getLang().equals(lang) && (variant == null
                        || (formatVariant != null && variant.equalsIgnoreCase(formatVariant.toString())))) {
                    if (!quadsSet && quadLangs.contains(lang)) {
                        r.quads = f;
                        quadsSet = true;
                        if (triplesSet)
                            break search;
                        else setSisterDefaults(r, SparqlResultType.Quads, EnumSet.of(SparqlResultType.Triples));
                    }
                    if (!triplesSet && tripleLangs.contains(lang)) {
                        r.triples = f;
                        triplesSet = true;
                        if (quadsSet)
                            break search;
                        else setSisterDefaults(r, SparqlResultType.Triples, EnumSet.of(SparqlResultType.Quads));
                    }
                }
            }

            if (!triplesSet) {
                for (RDFFormat f :
                        tripleFormats) {
                    RDFFormatVariant formatVariant = f.getVariant();
                    if (tripleLangs.contains(lang) && f.getLang().equals(lang) && (variant == null
                            || (formatVariant != null && variant.equalsIgnoreCase(formatVariant.toString())))) {
                        r.triples = f;
                        triplesSet = true;
                        if (quadsSet)
                            break search;
                        else setSisterDefaults(r, SparqlResultType.Triples, EnumSet.of(SparqlResultType.Quads));
                        break;
                    }
                }
            }
            if (!quadsSet) {
                for (RDFFormat f:
                quadFormats) {
                    RDFFormatVariant formatVariant = f.getVariant();
                    if (quadLangs.contains(lang) && f.getLang().equals(lang) && (variant == null
                            || (formatVariant != null && variant.equalsIgnoreCase(formatVariant.toString())))) {
                        r.quads = f;
                        quadsSet = true;
                        if (triplesSet)
                            break search;
                        else setSisterDefaults(r, SparqlResultType.Quads, EnumSet.of(SparqlResultType.Triples));
                        break;
                    }
                }
            }
        }
        return r;
    }

    @Override
    public Lang forAskResult() {
        return askResult;
    }

    @Override
    public Lang forBindings() {
        return bindings;
    }

    @Override
    public RDFFormat forTriples() {
        return triples;
    }

    @Override
    public RDFFormat forQuads() {
        return quads;
    }

    @Override
    public Lang forUnknown() {
        return unknown;
    }
}
