package org.aksw.jenax.arq.util.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.function.BiConsumer;

import org.aksw.commons.io.util.stream.InputStreamTransform;
import org.aksw.jenax.arq.util.lang.RDFLanguagesEx;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFWriterRegistry;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;

public class StreamingRDFConverter {
    public RDFConverterMetaData getMetaData() {
        return RDFConverterMetaDataJena.get();
    }

    public static InputStreamTransform converter(String inLangStr, String outFormatStr, String baseIri) {
        Lang inLang = RDFLanguagesEx.findLang(inLangStr);
        RDFFormat outFormat = RDFLanguagesEx.findRdfFormat(outFormatStr);
        return converter(inLang, outFormat, baseIri);
    }

    public static InputStreamTransform converter(Lang inLang, RDFFormat outFormat, String baseIri) {
        if (!RDFLanguages.isRegistered(inLang)) {
            throw new IllegalArgumentException("Input lang is not registered: " + inLang);
        }

        Lang outLang = outFormat.getLang();

        if (RDFLanguages.isQuads(inLang) && RDFLanguages.isTriples(outLang)) {
            // XXX Possibly warn of 'downcast'.
        }

        BiConsumer<InputStream, OutputStream> processor;
        if (StreamRDFWriter.registered(outFormat)) {
            processor = (in, out) -> convertStreaming(in, inLang, out, outFormat, baseIri);
        } else if (RDFWriterRegistry.contains(outFormat)) {
            processor = (in, out) -> convertMaterialized(in, inLang, out, outFormat, baseIri);
        } else {
            throw new IllegalArgumentException("Output format is not registered: " + outFormat);
        }

        return in -> {
            PipedOutputStream outPipe = new PipedOutputStream();
            PipedInputStream inPipe;
            try {
                int bufferSize = 64 * 1024; // 64 KB buffer
                inPipe = new PipedInputStream(outPipe, bufferSize);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Thread converterThread = new Thread(() -> {
                try (OutputStream out = outPipe) {
                    processor.accept(in, out);
                    out.flush();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            converterThread.start();

            InputStream r = new FilterInputStream(inPipe) {
                @Override
                public void close() throws IOException {
                    converterThread.interrupt();
                    try {
                        converterThread.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    super.close();
                }
            };

            return r;
        };
    }

//    public static void convertMaterializedTriples(InputStream in, Lang inLang, OutputStream out, RDFFormat outFormat, String baseIri) {
//        Graph graph = GraphFactory.createDefaultGraph();
//        RDFDataMgr.read(graph, in, baseIri, inLang);
//        RDFDataMgr.write(out, graph, outFormat);
//    }

    /**
     * Convert input to output via intermediate materialization.
     *
     * @implNote
     *   Uses a default DatasetGraph internally.
     *
     * @param in
     * @param inLang
     * @param out
     * @param outFormat
     * @param baseIri
     */
    public static void convertMaterialized(InputStream in, Lang inLang, OutputStream out, RDFFormat outFormat, String baseIri) {
        DatasetGraph dsg = DatasetGraphFactory.create();
        RDFDataMgr.read(dsg, in, baseIri, inLang);
        RDFDataMgr.write(out, dsg, outFormat);
    }

    public static void convertStreaming(InputStream in, Lang inLang, OutputStream out, RDFFormat outFormat, String baseIri) {
        StreamRDF streamWriter = StreamRDFWriter.getWriterStream(out, outFormat);
        RDFParser.create()
            .source(in)
            .lang(inLang)
            .base(baseIri)
            .build()
            .parse(streamWriter);
    }

    public static InputStream convert(InputStream in, Lang inLang, Lang outLang, String baseIri) throws IOException {
        RDFFormat outFormat = StreamRDFWriter.defaultSerialization(outLang);
        if (outFormat == null) {
            throw new IllegalArgumentException("No default serialization found for lang: " + outLang);
        }

        return convert(in, inLang, outFormat, baseIri);
    }

    public static InputStream convert(InputStream in, Lang inLang, RDFFormat outFormat, String baseIri) throws IOException {
        InputStreamTransform conv = converter(inLang, outFormat, baseIri);
        InputStream result = conv.apply(in);
        return result;
    }
}
