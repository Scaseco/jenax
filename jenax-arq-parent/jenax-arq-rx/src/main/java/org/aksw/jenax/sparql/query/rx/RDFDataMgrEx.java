package org.aksw.jenax.sparql.query.rx;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jena_sparql_api.rx.ModelFactoryEx;
import org.aksw.jenax.arq.dataset.orderaware.DatasetFactoryEx;
import org.aksw.jenax.arq.util.irixresolver.IRIxResolverUtils;
import org.aksw.jenax.arq.util.lang.RDFLanguagesEx;
import org.aksw.jenax.arq.util.streamrdf.StreamRDFWriterEx;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Graph;
import org.apache.jena.irix.IRIx;
import org.apache.jena.irix.IRIxResolver;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.resultset.ResultSetReaderRegistry;
import org.apache.jena.riot.system.AsyncParser;
import org.apache.jena.riot.system.AsyncParserBuilder;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sys.JenaSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Extensions to help open an InputStream of unknown content using probing against languages registered to the Jena riot system.
 * This includes languages based on triples, quads and result sets. Support for further types may be added in the future.
 *
 * @author Claus Stadler, Dec 18, 2018
 *
 */
public class RDFDataMgrEx {
    private static final Logger logger = LoggerFactory.getLogger(RDFDataMgrEx.class);

    static { JenaSystem.init(); }

    public static final List<Lang> DEFAULT_PROBE_LANGS = Collections.unmodifiableList(Arrays.asList(
            RDFLanguages.TRIG, // Subsumes turtle, nquads and ntriples
            RDFLanguages.JSONLD,
            RDFLanguages.RDFXML,
            RDFLanguages.RDFTHRIFT
            // RDFLanguages.TRIX
    ));


    public static String toString(Model model, RDFFormat rdfFormat) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RDFDataMgr.write(out, model, rdfFormat);
        return out.toString(StandardCharsets.UTF_8);
    }

    public static String toString(Dataset dataset, RDFFormat rdfFormat) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RDFDataMgr.write(out, dataset, rdfFormat);
        return out.toString(StandardCharsets.UTF_8);
    }



    public static boolean isStdIn(String filenameOrIri) {
        return "-".equals(filenameOrIri);
    }

    /**
     * Map a TypedInputStream's media type to a Lang
     *
     * @param tin
     * @return
     */
    public static Lang getLang(TypedInputStream tin) {
        ContentType ct = tin.getMediaType();
        Lang result = RDFLanguages.contentTypeToLang(ct);
        return result;
    }

    public static void read(Model model, TypedInputStream tin) {
        Lang lang = getLang(tin);
        RDFParser.create()
            .forceLang(lang)
            .source(tin.getInputStream())
            .base(tin.getBaseURI())
            .parse(model);
    }

    /**
     * Return a TypedInputStream whose underlying InputStream supports marks
     * If the original one already supports it it is returned as is.
     *
     * @param tin
     * @return
     */
    public static TypedInputStream forceBuffered(TypedInputStream tin) {
        TypedInputStream result = tin.markSupported()
                ? tin
                : wrapInputStream(new BufferedInputStream(tin.getInputStream()), tin);

        return result;
    }

    public static InputStream forceBuffered(InputStream in) {
        InputStream result = in.markSupported()
                ? in
                : new BufferedInputStream(in);

        return result;
    }

    /**
     * Wrap an InputStream as a TypedInputStream based on the attributes of the latter
     *
     * @param in
     * @param proto
     * @return
     */
    public static TypedInputStream wrapInputStream(InputStream in, TypedInputStream proto) {
        TypedInputStream result = new TypedInputStream(in, proto.getMediaType(), proto.getBaseURI());

        return result;
    }


    /**
     * Decode a given input stream based on a sequence of codec names.
     *
     * @param in
     * @param codecs
     * @param csf
     * @return
     * @throws CompressorException
     */
    public static InputStream decode(InputStream in, List<String> codecs, CompressorStreamFactory csf)
            throws CompressorException {
        InputStream result = in;
        for (String encoding : codecs) {
            result = csf.createCompressorInputStream(encoding, result, true);
        }
        return result;
    }

    public static OutputStream encode(OutputStream out, List<String> codecs, CompressorStreamFactory csf)
            throws CompressorException {
        OutputStream result = out;
        for (String encoding : codecs) {
            result = csf.createCompressorOutputStream(encoding, result);
        }
        return result;
    }

    /**
     * Given an input stream with mark support, attempt to create a decoded input stream.
     * The returned stream will be ready for further use with all detected encodings added to outEncodings.
     *
     * @param is An input stream that must have mark support
     * @param outEncodings Output argument. Detected encodings will be added to that list (if not null).
     */
    public static InputStream probeEncodings(InputStream is, List<String> outEncodings) throws IOException {
        if (!is.markSupported()) {
            throw new IllegalArgumentException("Encoding probing requires an input stream with mark support");
        }

        List<String> detectedEncodings = new ArrayList<>();
        CompressorStreamFactory csf = CompressorStreamFactory.getSingleton();
        InputStream nextIn = is;
        for (;;) {
            is.mark(1024 * 1024 * 1024);
            String encoding;
            try {
                encoding = CompressorStreamFactory.detect(is);
            } catch (CompressorException e) {
                break;
            } finally {
                is.reset();
            }
            detectedEncodings.add(encoding);
            if (outEncodings != null) {
                outEncodings.add(encoding);
            }

            try {
                // Only buffer the outermost stream; requires decoding from the base input stream
                nextIn = new BufferedInputStream(decode(is, detectedEncodings, csf));
            } catch (CompressorException e) {
                // Should not fail here because we applied detect() before
                throw new RuntimeException(e);
            }
        }
        return nextIn;
    }

    /**
     * Probe an input stream for any encodings (e.g. using compression codecs) and
     * its eventual content type.
     *
     * <pre>
     * try (InputStream in = ...) {
     *   EntityInfo entityInfo = probeEntityInfo(in, RDFDataMgrEx.DEFAULT_PROBE_LANGS);
     * }
     * </pre>
     *
     * @param in
     * @param candidates
     * @return
     * @throws IOException
     */
    public static RdfEntityInfo probeEntityInfo(InputStream in, Iterable<Lang> candidates) throws IOException {
        if (!in.markSupported()) {
            in = new BufferedInputStream(in);
        }
        // in.mark(1024 * 1024 * 1024);

        RdfEntityInfo result;
        try (InputStream is = in) {
            List<String> encodings = new ArrayList<>();
            InputStream nextIn = probeEncodings(is, encodings);
            try (TypedInputStream tis = RDFDataMgrEx.probeLang(nextIn, candidates)) {
                String contentType = tis.getContentType();
                String charset = tis.getCharset();

                result = ModelFactory.createDefaultModel().createResource().as(RdfEntityInfo.class);
                result.getContentEncodings().addAll(encodings);
                result.setContentType(contentType);
                result.setCharset(charset);

                // result = new EntityInfoImpl(encodings, contentType, charset);
            }
        }
        return result;
    }

    public static TypedInputStream probeLang(InputStream in, Iterable<Lang> candidates, Collection<Entry<Lang, Throwable>> errorCollector) {
        return probeLang(in, candidates, true, errorCollector);
    }

    /**
     * Determine the RDF content of the given input stream. The returned input stream buffers the given stream if needed.
     * Only the returned stream should be used after using this function.
     *
     * The following example shows how to obtain a Lang from the probing result:
     * <pre>
     * TypedInputStream tin = RDFDataMgrEx.probeLang(in, RDFDataMgrEx.DEFAULT_PROBE_LANGS);
     * Lang lang = RDFLanguages.contentTypeToLang(tis.getContentType());
     * </pre>
     */
    public static TypedInputStream probeLang(InputStream in, Iterable<Lang> candidates) {
        return probeLang(in, candidates, new ArrayList<>());
    }

    /**
     * Probe the content of the input stream against a given set of candidate languages.
     * Wraps the input stream as a BufferedInputStream and can thus also probe on STDIN.
     * This is also the reason why the method does not take an InputStream supplier as argument.
     *
     * The result is a TypedInputStream which combines the BufferedInputStream with content
     * type information
     *
     *
     * @param in
     * @param candidates
     * @param tryAllCandidates If true do not accept the first successful candidate; instead try all candidates and pick the one that yields most data
     *
     * @return
     */
    public static TypedInputStream probeLang(
            InputStream in,
            Iterable<Lang> candidates,
            boolean tryAllCandidates,
            Collection<Entry<Lang, Throwable>> errorCollector) {
        if (!in.markSupported()) {
            throw new IllegalArgumentException("Language probing requires an input stream with mark support");
        }

//        BufferedInputStream bin = new BufferedInputStream(in);

        // Here we rely on the VM/JDK not allocating the buffer right away but only
        // using this as the max buffer size
        // 1GB should be safe enough even for cases with huge literals such as for
        // large spatial geometries (I encountered some around ~50MB)
        in.mark(1 * 1024 * 1024 * 1024);

        Multimap<Long, Lang> successCountToLang = ArrayListMultimap.create();
        for(Lang cand : candidates) {
            @SuppressWarnings("resource")
            CloseShieldInputStream wbin = new CloseShieldInputStream(in);

            AsyncParserBuilder builder = AsyncParser.of(wbin, cand, null)
                    .mutateSources(parser -> parser.errorHandler(ErrorHandlerFactory.errorHandlerSimple()))
                    .setChunkSize(100).setQueueSize(10);

            //bin.mark(Integer.MAX_VALUE >> 1);
            Stream<?> flow;
            if (RDFLanguages.isQuads(cand)) {
                flow = builder.streamQuads();
            } else if (RDFLanguages.isTriples(cand)) {
                flow = builder.streamTriples();
            } else if (ResultSetReaderRegistry.isRegistered(cand)) {
                flow = RDFDataMgrRx.createFlowableBindings(() -> wbin, cand).blockingStream();
            } else {
                logger.warn("Skipping probing of unknown Lang: " + cand);
                continue;
            }

            // Stopwatch sw = Stopwatch.createStarted();

            // TODO If there is a syntax error within the first n items
            // then the format won't be recognized at all
            // We should add an indirection layer that allows to configure the prober
            // and query its result before allowing the client to obtain the input stream
            int n = 100;
            try (Stream<?> s = flow) {
                Iterator<?> it = s.iterator();
                long count = 0;
                for (; count < n && it.hasNext(); ++count) {
                    it.next();
                }
                successCountToLang.put(count, cand);

                logger.debug("Number of items parsed by content type probing for " + cand + ": " + count);
            } catch(Exception e) {
                logger.debug("Failed to probe with format " + cand, e);
                if (errorCollector != null) {
                    errorCollector.add(Pair.of(cand, e));
                }
                continue;
            } finally {
                // logger.debug("Probing format " + cand + " took " + sw.elapsed(TimeUnit.MILLISECONDS));

                try {
                    in.reset();
                } catch (IOException x) {
                    throw new RuntimeException(x);
                }
            }

            if (!tryAllCandidates) {
                break;
            }
        }

        Entry<Long, Lang> bestCand = successCountToLang.entries().stream()
            .sorted((a, b) -> b.getKey().compareTo(a.getKey()))
            .findFirst()
            .orElse(null);

        ContentType bestContentType = bestCand == null ? null : bestCand.getValue().getContentType();
        TypedInputStream result = new TypedInputStream(in, bestContentType);

        return result;
    }

    public static TypedInputStream probeLang(
            InputStream in,
            Iterable<Lang> candidates,
            boolean tryAllCandidates) {
        return probeLang(in, candidates, tryAllCandidates, new ArrayList<>());
    }
//    public static TypedInputStream probeLang(
//            InputStream in,
//            Iterable<Lang> candidates,
//            boolean tryAllCandidates) {
//        if (!in.markSupported()) {
//            throw new IllegalArgumentException("Language probing requires an input stream with mark support");
//        }
//
////        BufferedInputStream bin = new BufferedInputStream(in);
//
//        // Here we rely on the VM/JDK not allocating the buffer right away but only
//        // using this as the max buffer size
//        // 1GB should be safe enough even for cases with huge literals such as for
//        // large spatial geometries (I encountered some around ~50MB)
//        in.mark(1 * 1024 * 1024 * 1024);
//
//        Multimap<Long, Lang> successCountToLang = ArrayListMultimap.create();
//        for(Lang cand : candidates) {
//            @SuppressWarnings("resource")
//            CloseShieldInputStream wbin = new CloseShieldInputStream(in);
//
//            //bin.mark(Integer.MAX_VALUE >> 1);
//            Flowable<?> flow;
//            if (RDFLanguages.isQuads(cand)) {
//                flow = RDFDataMgrRx.createFlowableQuads(() -> wbin, cand, null);
//            } else if (RDFLanguages.isTriples(cand)) {
//                flow = RDFDataMgrRx.createFlowableTriples(() -> wbin, cand, null);
//            } else if (ResultSetReaderRegistry.isRegistered(cand)) {
//                flow = RDFDataMgrRx.createFlowableBindings(() -> wbin, cand);
//            } else {
//                logger.warn("Skipping probing of unknown Lang: " + cand);
//                continue;
//            }
//
//            // Stopwatch sw = Stopwatch.createStarted();
//
//            // TODO If there is a syntax error within the first n items
//            // then the format won't be recognized at all
//            // We should add an indirection layer that allows to configure the prober
//            // and query its result before allowing the client to obtain the input stream
//            int n = 100;
//            try {
//                long count = flow.take(n)
//                        .count()
//                        .blockingGet();
//
//                successCountToLang.put(count, cand);
//
//                logger.debug("Number of items parsed by content type probing for " + cand + ": " + count);
//            } catch(Exception e) {
//                logger.debug("Failed to probe with format " + cand, e);
//                continue;
//            } finally {
//                // logger.debug("Probing format " + cand + " took " + sw.elapsed(TimeUnit.MILLISECONDS));
//
//                try {
//                    in.reset();
//                } catch (IOException x) {
//                    throw new RuntimeException(x);
//                }
//            }
//
//            if (!tryAllCandidates) {
//                break;
//            }
//        }
//
//        Entry<Long, Lang> bestCand = successCountToLang.entries().stream()
//            .sorted((a, b) -> b.getKey().compareTo(a.getKey()))
//            .findFirst()
//            .orElse(null);
//
//        ContentType bestContentType = bestCand == null ? null : bestCand.getValue().getContentType();
//        TypedInputStream result = new TypedInputStream(in, bestContentType);
//
//        return result;
//    }


    public static void peek(InputStream in) {
        in.mark(1 * 1024 * 1024 * 1024);

        try {
            System.err.println("GOT:");
            System.err.println(IOUtils.toString(in));
            System.err.println("DONE");
            in.reset();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Attempts to open the given src and probe for the content type
     * Src may be '-' but not NULL in order to refer to STDIN.
     *
     * @param src
     * @param probeLangs
     * @return
     */
    public static TypedInputStream open(String src, Iterable<Lang> probeLangs, Collection<Entry<Lang, Throwable>> errorCollector) {
        Objects.requireNonNull(src);

        boolean useStdIn = isStdIn(src);

        TypedInputStream result;
        if(useStdIn) {
            // Use the close shield to prevent closing stdin on .close()
            // TODO Investigate if this is redundant; RDFDataMgr might already do it

            // FIXME Does not work for encoded streams; for those we would have to go through
            // Jena's StreamManager
            result = probeLang(new BufferedInputStream(System.in), probeLangs);
        } else {
            result = Objects.requireNonNull(RDFDataMgr.open(src), "Could not create input stream from " + src);

            result = probeForSpecificLang(result, probeLangs, errorCollector);
        }

        return result;
    }

    public static TypedInputStream open(String src, Iterable<Lang> probeLangs) {
        return open(src, probeLangs, new ArrayList<>());
    }

    /** open via nio */
    public static TypedInputStream open(Path path, Iterable<Lang> probeLangs, Collection<Entry<Lang, Throwable>> errorCollector) {
        InputStream in;
        try {
            in = Files.newInputStream(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return probeForSpecificLang(new TypedInputStream(in, (ContentType)null), probeLangs, errorCollector);
    }

    public static TypedInputStream open(Path path, Iterable<Lang> probeLangs) {
        return open(path, probeLangs, null);
    }

    public static TypedInputStream probeForSpecificLang(TypedInputStream result, Iterable<Lang> probeLangs, Collection<Entry<Lang, Throwable>> errorCollector) {
        // TODO Should we rely on the content type returned by RDFDataMgr? It may be based on e.g. a file extension
        // rather than the actual content - so we may be fooled here

        // Expand the languages such that when  probing for languages such as turtle or trig then we also accept ntriples or nquads
        Set<Lang> expandedLangs = RDFLanguagesEx.expandWithSubLangs(probeLangs);
        ContentType mediaType = result.getMediaType();
        if (mediaType != null) {
            // Check if the detected content type matches the ones we are probing for
            // If not then unset the content type and probe the content again
            String mediaTypeStr = mediaType.toHeaderString();
            boolean mediaTypeInProbeLangs = expandedLangs.stream()
                    .anyMatch(lang -> RDFLanguagesEx.getAllContentTypes(lang).contains(mediaTypeStr));

            if (!mediaTypeInProbeLangs) {
                mediaType = null;
            }
        }

        if(mediaType == null) {
            result = probeLang(forceBuffered(result.getInputStream()), probeLangs, errorCollector);
        }
        return result;
    }

//    public static RDFIterator<Triple> createIteratorTriples(PrefixMapping prefixMapping, InputStream in, Lang lang) {
//        InputStream combined = prependWithPrefixes(in, prefixMapping);
//        RDFIterator<Triple> it = RDFDataMgrRx.createIteratorTriples(combined, lang, null, (thread, throwable) -> {}, thread -> {});
//        return it;
//    }
//
//
//    public static RDFIterator<Quad> createIteratorQuads(PrefixMapping prefixMapping, InputStream in, Lang lang) {
//        InputStream combined = prependWithPrefixes(in, prefixMapping);
//        RDFIterator<Quad> it = RDFDataMgrRx.createIteratorQuads(combined, lang, null, (thread, throwable) -> {}, thread -> {});
//        return it;
//    }

    public static Dataset parseTrigAgainstDataset(Dataset dataset, PrefixMapping prefixMapping, InputStream in) {
        // Add namespaces from the spec
        // Apparently Jena does not support parsing against
        // namespace prefixes previously declared in the target model
        // Therefore we serialize the prefix declarations and prepend them to the
        // input stream of the dataset
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		Model tmp = ModelFactory.createDefaultModel();
//		tmp.setNsPrefixes(prefixMapping);
//		RDFDataMgr.write(baos, tmp, Lang.TURTLE);
////		System.out.println("Prefix str: " + baos.toString());
//
//		InputStream combined = new SequenceInputStream(
//				new ByteArrayInputStream(baos.toByteArray()), in);
//
        InputStream combined = prependWithPrefixes(in, prefixMapping);
        RDFDataMgr.read(dataset, combined, Lang.TRIG);

        return dataset;
    }


    /**
     * Parse the input stream as turtle, thereby prepending a serialization of the given prefix mapping.
     * This is a workaround for Jena's riot framework - especially RDFParser - apparently not supporting
     * injecting a prefix mapping.
     *
     *
     * @param model
     * @param prefixMapping
     * @param in
     * @return
     */
    public static Model parseTurtleAgainstModel(Model model, PrefixMapping prefixMapping, InputStream in) {
        // Add namespaces from the spec
        // Apparently Jena does not support parsing against
        // namespace prefixes previously declared in the target model
        // Therefore we serialize the prefix declarations and prepend them to the
        // input stream of the dataset
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Model tmp = ModelFactory.createDefaultModel();
        tmp.setNsPrefixes(prefixMapping);
        RDFDataMgr.write(baos, tmp, Lang.TURTLE);

        InputStream combined = new SequenceInputStream(
                new ByteArrayInputStream(baos.toByteArray()), in);

        RDFDataMgr.read(model, combined, Lang.TURTLE);

        return model;
    }

    /**
     * Convenience method to prepend prefixes to an input stream (in turtle syntax)
     *
     * @param in
     * @param prefixMapping
     * @return
     */
    public static InputStream prependWithPrefixes(InputStream in, PrefixMapping prefixMapping) {
         return prependWithPrefixes(in, prefixMapping, RDFFormat.TURTLE_PRETTY);
    }

    /**
     * Convenience method to prepend prefixes to an input stream (in a given format)
     *
     * @param in
     * @param prefixMapping
     * @return
     */
    public static InputStream prependWithPrefixes(InputStream in, PrefixMapping prefixMapping, RDFFormat fmt) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Model tmp = ModelFactory.createDefaultModel();
        tmp.setNsPrefixes(prefixMapping);
        RDFDataMgr.write(baos, tmp, fmt);
//		System.out.println("Prefix str: " + baos.toString());

        InputStream combined = new SequenceInputStream(
                new ByteArrayInputStream(baos.toByteArray()), in);

        return combined;
    }


    public static TypedInputStream prependWithPrefixes(TypedInputStream in, PrefixMapping prefixMapping) {
        InputStream combined = prependWithPrefixes(in.getInputStream(), prefixMapping);

        TypedInputStream result = new TypedInputStream(combined, in.getMediaType(), in.getBaseURI());
        return result;
    }

    /** Return a preconfigured parser builder that retains blank node ids and relative IRIs */
    public static RDFParserBuilder newParserBuilderForReadAsGiven(String baseIri) {
        IRIxResolver resolver = IRIxResolverUtils.newIRIxResolverAsGiven(baseIri);

        return RDFParser.create()
            .resolver(resolver)
            .context(null)
            .base(null)
            .errorHandler(RDFDataMgrRx.dftErrorHandler())
            .labelToNode(RDFDataMgrRx.createLabelToNodeAsGivenOrRandom());
    }


    public static Graph readAsGiven(Graph graph, String uri) {
        newParserBuilderForReadAsGiven(null).source(uri).parse(graph);
        return graph;
    }

    public static Model readAsGiven(Model model, String uri) {
        newParserBuilderForReadAsGiven(null).source(uri).parse(model);
        return model;
    }

    public static DatasetGraph readAsGiven(DatasetGraph datasetGraph, String uri) {
        newParserBuilderForReadAsGiven(null).source(uri).parse(datasetGraph);
        return datasetGraph;
    }

    public static Dataset readAsGiven(Dataset dataset, String uri) {
        newParserBuilderForReadAsGiven(null).source(uri).parse(dataset);
        return dataset;
    }

    public static Model loadModelAsGiven(String uri) {
        Model result = ModelFactoryEx.createInsertOrderPreservingModel();
        readAsGiven(result, uri);
        return result;
    }

    public static DatasetGraph readAsGiven(DatasetGraph datasetGraph, String uri, String baseIri) {
        newParserBuilderForReadAsGiven(baseIri).source(uri).parse(datasetGraph);
        return datasetGraph;
    }

    public static Dataset readAsGiven(Dataset dataset, String uri, String baseIri) {
        readAsGiven(dataset.asDatasetGraph(), uri, baseIri);
        return dataset;
    }

    public static DatasetGraph readAsGiven(DatasetGraph datasetGraph, InputStream in, Lang lang) {
        newParserBuilderForReadAsGiven(null).source(in).lang(lang).build().parse(datasetGraph);
        return datasetGraph;
    }

    public static Dataset readAsGiven(Dataset dataset, InputStream in, Lang lang) {
        readAsGiven(dataset.asDatasetGraph(), in, lang);
        return dataset;
    }

    public static Dataset loadDatasetAsGiven(String uri, String baseIri) {
        Dataset result = DatasetFactoryEx.createInsertOrderPreservingDataset();
        readAsGiven(result, uri, baseIri);
        return result;
    }

    public static void writeAsGiven(OutputStream out, Model model, RDFFormat rdfFormat, String baseIri) {
        writeAsGiven(out, DatasetFactory.wrap(model), rdfFormat, baseIri);
    }

    public static void writeAsGiven(OutputStream out, Dataset dataset, RDFFormat rdfFormat, String baseIri) {
        writeAsGiven(out, dataset.asDatasetGraph(), rdfFormat, baseIri);
    }

    // TODO Implement; A variant of write that accepts a context; allows e.g. disabling writing out base IRIs
    public static void writeAsGiven(OutputStream out, DatasetGraph datasetGraph, RDFFormat rdfFormat, String baseIri) {
        Context cxt = RIOT.getContext().copy();
        cxt.setTrue(RIOT.symTurtleOmitBase);

        IRIx irix = baseIri == null
                ? null
                : IRIx.create(baseIri) // IRIxResolverUtils.newIRIxAsGiven(baseIri)
                ;

        StreamRDF writer = StreamRDFWriterEx.getWriterStream(
                out,
                rdfFormat,
                cxt,
                null,
                irix,
                null,
                // NodeToLabel.createBNodeByLabelAsGiven(),
                true
        );


        writer.start();
        StreamRDFOps.sendDatasetToStream(datasetGraph, writer);
        writer.finish();


//        RDFWriter writer = RDFWriter
//            .create(dataset)
//            .base(baseIri)
//            .context(cxt)
//            .format(rdfFormat)
//            .build();
//
//        if (writer instanceof WriterStreamRDF) {
//            // WriterStreamRDFBaseUtils.setNodeToLabel(writer, RDFDataMgrRx.createLabelToNodeAsGivenOrRandom());
//        }
            //.output(out);

        // RDFDataMgr.write
        // Context.set(RIOT.symTurtleOmitBase);
        // RIOT.multilineLiterals
        // TODO
    }

    /**
     * Serialize a dataset in memory and return its deserialized version.
     *
     * @param dataset The input dataset.
     * @param rdfFormat The serialization format. Its lang is used for deserialization.
     * @param result The dataset to write to. If null then a new default dataset is created.
     * @return The dataset obtained from deserialization.
     */
    public static Dataset printParseRoundtrip(Dataset dataset, RDFFormat rdfFormat, Dataset result) {
        if (result == null) {
            result = DatasetFactory.create();
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            RDFDataMgr.write(out, dataset, rdfFormat);
            try (ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray())) {
                RDFDataMgr.read(result, in, rdfFormat.getLang());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * Serialize a model in memory and return its deserialized version.
     *
     * @param model The input model.
     * @param rdfFormat The serialization format. Its lang is used for deserialization.
     * @param result The model to write to. If null then a new default model is created.
     * @return The model obtained from deserialization.
     */
    public static Model printParseRoundtrip(Model model, RDFFormat rdfFormat, Model result) {
        if (result == null) {
            result = ModelFactory.createDefaultModel();
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            RDFDataMgr.write(out, model, rdfFormat);
            try (ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray())) {
                RDFDataMgr.read(result, in, rdfFormat.getLang());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}
