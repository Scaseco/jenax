package org.aksw.shellgebra.algebra.stream.transform;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.function.Function;

import org.apache.commons.compress.utils.CloseShieldFilterInputStream;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;

public class StreamingRDFConverter {

    public static InputStream convert(InputStream in, Lang inLang, Lang outLang, String baseIri) throws IOException {
        RDFFormat outFormat = StreamRDFWriter.defaultSerialization(outLang);
        if (outFormat == null) {
            throw new IllegalArgumentException("No default serialization found for lang: " + outLang);
        }

        return convert(in, inLang, outFormat, baseIri);
    }

    public static Function<InputStream, InputStream> converter(Lang inLang, RDFFormat outFormat, String baseIri) {
        if (!RDFLanguages.isRegistered(inLang)) {
            throw new IllegalArgumentException("Input lang is not registered: " + inLang);
        }

        if (!StreamRDFWriter.registered(outFormat)) {
            throw new IllegalArgumentException("Output format is not registered: " + outFormat);
        }

        return in -> {
            PipedOutputStream outPipe = new PipedOutputStream();
            PipedInputStream inPipe;
            try {
                inPipe = new PipedInputStream(outPipe, 64 * 1024); // 64 KB buffer
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Thread converterThread = new Thread(() -> {
                try (OutputStream out = outPipe) {
                    StreamRDF streamWriter = StreamRDFWriter.getWriterStream(out, outFormat);

                    RDFParser.create()
                        .source(in)
                        .lang(inLang)
                        .base(baseIri)
                        .build()
                        .parse(streamWriter);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            converterThread.start();

            InputStream r = new CloseShieldFilterInputStream(inPipe) {
                @Override
                public void close() throws IOException {
                    converterThread.interrupt();
                    super.close();
                }
            };

            return r;
        };
    }

    public static InputStream convert(InputStream in, Lang inLang, RDFFormat outFormat, String baseIri) throws IOException {
        Function<InputStream, InputStream> conv = converter(inLang, outFormat, baseIri);
        InputStream result = conv.apply(in);
        return result;
    }
}
