package org.aksw.jenax.arq.util.streamrdf;

import java.io.OutputStream;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.jena.graph.Graph;
import org.apache.jena.irix.IRIx;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.out.NodeToLabel;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.riot.system.StreamRDFWrapper;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.riot.system.SyntaxLabels;
import org.apache.jena.riot.writer.WriterStreamRDFBase;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;

/**
 * Special purpose RDF writer generation.
 * Especially blank nodes are preserved as given.
 *
 * @author raven
 *
 */
public class StreamRDFWriterEx {

    public static StreamRDF getWriterStream(
            OutputStream out,
            RDFFormat rdfFormat,
            Context context) {
        return getWriterStream(out, rdfFormat, context, null, null, null, null);
    }

    public static StreamRDF getWriterStream(
            OutputStream out,
            Lang lang,
            Context context) {
    	StreamRDF rawWriter = StreamRDFWriter.getWriterStream(out, lang, context);
    	StreamRDF result = enhanceWriterAsGiven(rawWriter, lang);
    	return result;
    }


    public static void writeAsGiven(DatasetGraph dg, OutputStream out, RDFFormat format, Context cxt,
            Function<StreamRDF, StreamRDF> applyWrapper) {
        writeAsGiven(dg, StreamRDFOps::sendDatasetToStream, out, format, cxt, applyWrapper);
    }

    public static void writeAsGiven(Graph graph, OutputStream out, RDFFormat format, Context cxt,
            Function<StreamRDF, StreamRDF> applyWrapper) {
        writeAsGiven(graph, StreamRDFOps::sendGraphToStream, out, format, cxt, applyWrapper);
    }

    /**
    *
    * @param dg The dataset
    * @param out The output stream
    * @param format The RDF format
    * @param cxt The context for riot
    * @param applyWrapper A callback for optionally wrapping the internal streamRdf (e.g. deferring output while sampling used prefixes); may be null.
    */

    public static <T> void writeAsGiven(
            T data, BiConsumer<T, StreamRDF> sendToStream,
            OutputStream out, RDFFormat format, Context cxt,
            Function<StreamRDF, StreamRDF> applyWrapper) {
        StreamRDF streamRdf = getWriterStream(out, format, cxt);

        // streamRdf = new StreamRDFDeferred(streamRdf, true, DefaultPrefixes.get(), 1000, 1000, null);
        if (applyWrapper != null) {
            streamRdf = applyWrapper.apply(streamRdf);
        }

        streamRdf.start();
        sendToStream.accept(data, streamRdf);
        streamRdf.finish();
    }
    /**
     * Create a StreamRDF writer with extended options.
     *
     * @param out The output stream.
     * @param rdfFormat The rdf format; a registration of a streamable writer must exist for it
     * @param context The context passed to the writer creation.
     * @param fixedPrefixes If non-null, only this set prefixes will be written out;
     *            the returned writer will ignore prefix events.
     * @param nodeToLabel The blank node strategy. If null, <b>blank nodes are preserved as given</b>.
     * @param mapQuadsToTriplesForTripleLangs If false, the writer for a quad language will ignore triples.
     *            If true, triples become quads in the default graph. Defaults to true.
     * @return A writer according to parameterization.
     */
    public static StreamRDF getWriterStream(
            OutputStream out,
            RDFFormat rdfFormat,
            Context context,
            PrefixMapping fixedPrefixes,
            IRIx irix,
            NodeToLabel nodeToLabel,
            Boolean mapQuadsToTriplesForTripleLangs
    ) {
        StreamRDF rawWriter = StreamRDFWriter.getWriterStream(out, rdfFormat, context);
    	Lang lang = rdfFormat.getLang();

    	StreamRDF result = enhanceWriter(rawWriter, lang, fixedPrefixes, irix, nodeToLabel, mapQuadsToTriplesForTripleLangs);
    	return result;
    }


    public static StreamRDF enhanceWriterAsGiven(StreamRDF rawWriter, Lang lang) {
    	StreamRDF result = enhanceWriter(rawWriter, lang, null, null, null, true);
    	return result;
    }

	public static StreamRDF enhanceWriter(StreamRDF rawWriter, Lang lang,
			PrefixMapping fixedPrefixes, IRIx irix, NodeToLabel nodeToLabel,
			Boolean mapQuadsToTriplesForTripleLangs) {
		StreamRDF coreWriter = StreamRDFUtils.unwrap(rawWriter);

        // Retain blank nodes as given
        if (coreWriter instanceof WriterStreamRDFBase) {
            WriterStreamRDFBase tmp = (WriterStreamRDFBase)coreWriter;

            IRIx effectiveIrix = irix == null
                    ? IRIx.create("") // IRIxResolverUtils.newIRIxAsGiven("")
                    : irix;

            NodeToLabel effectiveNodeToLabel = nodeToLabel == null
                    ? SyntaxLabels.createNodeToLabelAsGiven()
                    : nodeToLabel;

            WriterStreamRDFBaseUtils.setNodeToLabel(tmp, effectiveNodeToLabel);
            WriterStreamRDFBaseUtils.updateFormatter(tmp);
            WriterStreamRDFBaseUtils.setNodeFormatterIRIx(tmp, effectiveIrix);

            if (fixedPrefixes != null) {
                PrefixMap pm = WriterStreamRDFBaseUtils.getPrefixMap(tmp);
                for (Map.Entry<String, String> e : fixedPrefixes.getNsPrefixMap().entrySet()) {
                    pm.add(e.getKey(), e.getValue());
                }

                rawWriter = StreamRDFUtils.wrapWithoutPrefixDelegation(rawWriter);
            }
        }

        if (Boolean.TRUE.equals(mapQuadsToTriplesForTripleLangs) && RDFLanguages.isTriples(lang)) {
            rawWriter = new StreamRDFWrapper(rawWriter) {
                @Override
                public void quad(Quad quad) {
                    super.triple(quad.asTriple());
                }
            };
        }

        return rawWriter;
	}

//  public static StreamRDF getWriterAsGiven(OutputStream out, RDFFormat rdfFormat, Context context) {
//	return getWriterStream(out, rdfFormat, context, null, )
//    getWriterStream(out, rdfFormat, );
//
//    StreamRDF rawWriter = StreamRDFWriter.getWriterStream(out, rdfFormat, context);
//    StreamRDF coreWriter = StreamRDFUtils.unwrap(rawWriter);
//
//    // Retain blank nodes as given
//    if (coreWriter instanceof WriterStreamRDFBase) {
//        WriterStreamRDFBase tmp = (WriterStreamRDFBase)coreWriter;
//        WriterStreamRDFBaseUtils.setNodeFormatterIRIx(tmp, IRIxResolverUtils.newIRIxAsGiven(""));
//        WriterStreamRDFBaseUtils.setNodeToLabel(tmp, SyntaxLabels.createNodeToLabelAsGiven());
//
//    }
//
//    return rawWriter;
//}

}
