package org.aksw.jenax.sparql.query.rx;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.aksw.commons.rx.util.FlowableEx;
import org.aksw.commons.rx.util.FlowableUtils;
import org.aksw.jena_sparql_api.rx.AllocScopePolicy;
import org.aksw.jenax.arq.dataset.orderaware.DatasetGraphFactoryEx;
import org.aksw.jenax.arq.util.irixresolver.IRIxResolverUtils;
import org.aksw.jenax.arq.util.node.BlankNodeAllocatorAsGivenOrRandom;
import org.aksw.jenax.arq.util.quad.DatasetUtils;
import org.aksw.jenax.arq.util.quad.QuadPatternUtils;
import org.aksw.jenax.arq.util.streamrdf.StreamRDFWriterEx;
import org.aksw.jenax.sparql.query.rx.StreamUtils.QuadEncoderDistinguish;
import org.aksw.jenax.sparql.rx.op.FlowOfQuadsOps;
import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.ResultSetMgr;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.system.AsyncParser;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.FactoryRDF;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.ParserProfileStd;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Context;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.core.Maybe;

/**
 * Reactive extensions of RDFDataMgr
 *
 * @author Claus Stadler, Nov 12, 2018
 *
 */
public class RDFDataMgrRx {
    public static Flowable<Triple> createFlowableTriples(String filenameOrURI, Lang lang, String baseIRI) {
        return createFlowableTriples(() -> RDFDataMgr.open(filenameOrURI), lang, baseIRI);
    }

    /**
     * Create a Flowable for a SPARQL result set backed by a file
     *
     * @param filenameOrURI
     * @param lang
     * @return
     */
    public static Flowable<Binding> createFlowableBindings(String filenameOrURI, Lang lang) {
        return createFlowableBindings(() -> RDFDataMgr.open(filenameOrURI), lang);
    }

    /**
     * Create a Flowable for a SPARQL result set backed by an supplier of input streams
     *
     * @param filenameOrURI
     * @param lang
     * @return
     */
    public static Flowable<Binding> createFlowableBindings(Callable<InputStream> inSupp, Lang lang) {
        return createFlowableBindings(() -> {
            ContentType ct = lang.getContentType();
            InputStream in = inSupp.call();
            return new TypedInputStream(in, ct);
        });
    }


    /**
     * Create a Flowable for a SPARQL result set backed by a supplier of TypedInputStream
     *
     * @param filenameOrURI
     * @param lang
     * @return
     */
    public static Flowable<Binding> createFlowableBindings(Callable<TypedInputStream> inSupp) {
        Flowable<Binding> result = FlowableUtils.createFlowableFromResource(
                inSupp,
                in -> {
                    Lang lang = RDFLanguages.contentTypeToLang(in.getContentType());
                    ResultSet rs = ResultSetMgr.read(in.getInputStream(), lang);
                    return rs;
                },
                ResultSet::hasNext,
                ResultSet::nextBinding,
                in -> { try { in.close(); } catch (Exception e) { throw new RuntimeException(e); } }
            );

        return result;
    }

    public static Flowable<Triple> createFlowableTriples(Callable<InputStream> inSupplier, Lang lang, String baseIRI) {
        return FlowableEx.fromIteratorSupplier(() -> AsyncParser.of(inSupplier.call(), lang, baseIRI)
                .mutateSources(source -> applyParserDefaults(source))
                .asyncParseTriples(), IteratorCloseable::close);
    }

    public static Flowable<Resource> createFlowableResources(String filenameOrURI, Lang lang, String baseIRI) {
        return createFlowableResources(() -> RDFDataMgr.open(filenameOrURI), lang, baseIRI);
    }

    public static Flowable<Dataset> createFlowableDatasets(String filenameOrURI, Lang lang, String baseIRI) {
        return createFlowableDatasets(() -> RDFDataMgr.open(filenameOrURI), lang, baseIRI);
    }

    /**
     * Label to node strategy that passes existing labels on as given
     * but allocation of fresh nodes uses a pair comprising a jvm-global random value and an increment.
     * (i.e. incremental numbers scoped within some random value)
     *
     * This strategy is needed when processing RDF files in splits such as with Apache Spark:
     * Any mentioned labels should be retaine globally, but fresh nodes allocated for the splits must not clash.
     *
     * @return
     */
    public static LabelToNode createLabelToNodeAsGivenOrRandom() {
        return new LabelToNode(
                new AllocScopePolicy(),
                new Alloc(BlankNodeAllocatorAsGivenOrRandom.getGlobalInstance()));
    }


    public static ErrorHandler dftErrorHandler() {
        return ErrorHandlerFactory.errorHandlerWarn;
    }

    public static ParserProfile dftProfile() {
        return permissiveProfile();
    }

    public static RDFParserBuilder applyParserDefaults(RDFParserBuilder builder) {
        return builder
            .resolver(IRIxResolverUtils.newIRIxResolverAsGiven())
            // Disabling checking does not seem to give a significant performance gain
            // For a 3GB Trig file parsing took ~1:45 min +- 5 seconds either way
            //.checking(false)
            .errorHandler(dftErrorHandler())
            .labelToNode(createLabelToNodeAsGivenOrRandom());
    }

    public static ParserProfile createParserProfile(FactoryRDF factory, ErrorHandler errorHandler, boolean checking) {
        return new ParserProfileStd(factory,
                                    errorHandler,
                                    // IRIxResolver.create(IRIs.getSystemBase()).build(),
                                    // IRIxResolver.create().noBase().allowRelative(true).build(),
                                    IRIxResolverUtils.newIRIxResolverAsGiven(),
                                    PrefixMapFactory.create(),
                                    RIOT.getContext().copy(),
                                    checking,
                                    false);
    }

    public static ParserProfile strictProfile() {
        return createParserProfile(RiotLib.factoryRDF(
                createLabelToNodeAsGivenOrRandom()),
                ErrorHandlerFactory.errorHandlerExceptionOnError(),
                true);
    }

    public static ParserProfile permissiveProfile() {
        return createParserProfile(RiotLib.factoryRDF(
                createLabelToNodeAsGivenOrRandom()),
                ErrorHandlerFactory.errorHandlerWarn,
                false);
    }

    /**
     * Adaption from RDFDataMgr.createIteratorQuads that waits for
     * data on the input stream indefinitely and allows for thread handling
     *
     * Creates an iterator over parsing of quads
     * @param input Input Stream
     * @param lang Language
     * @param baseIRI Base IRI
     * @return Iterator over the quads
     */
//    public static RDFIterator<Triple> createIteratorTriples(
//            InputStream input,
//            Lang lang,
//            String baseIRI,
//            int bufferSize, boolean fair, int pollTimeout, int maxPolls,
//            Consumer<Thread> th,
//            UncaughtExceptionHandler eh) {
//        // Special case N-Quads, because the RIOT reader has a pull interface
//        if ( RDFLanguages.sameLang(RDFLanguages.NTRIPLES, lang) ) {
//            return new RDFIteratorFromIterator<Triple>(Iter.onCloseIO(
//                RiotParsers.createIteratorNTriples(input, null, RDFDataMgrRx.dftProfile()),
//                input), baseIRI);
//        }
//        // Otherwise, we have to spin up a thread to deal with it
//        RDFIteratorFromPipedRDFIterator<Triple> it = new RDFIteratorFromPipedRDFIterator<>(bufferSize, fair, pollTimeout, maxPolls);
//        PipedTriplesStream out = new PipedTriplesStream(it);
//
//        // We need to handle finish ourself in order to pass any raised exception to rxjava
//        StreamRDF ignoreFinishWrapper = new StreamRDFWrapper(out) {
//            @Override public void finish() {}
//        };
//
//        Thread t = new Thread(()-> {
//            try {
//                // Invoke start on the sink so that the consumer knows the producer thread
//                // It appears otherwise the producer thread can get interrupted before
//                // the consumer gets to know the producer (which happens on start)
//                out.start();
//                parseFromInputStream(ignoreFinishWrapper, input, baseIRI, lang, null);
//            } catch(Exception e) {
//                // Ensure the exception handler is run before any
//                // thread.join() waiting for this thread
//                eh.uncaughtException(Thread.currentThread(), e);
//            } finally {
//                try {
//                    out.finish();
//                } catch (Exception e2) {
//                    // Silently ignore failure on finish due to closed consumer
//                }
//            }
//        });
//        th.accept(t);
//        t.start();
//        return it;
//    }

    public static void parseFromInputStream(StreamRDF destination, InputStream in, String baseUri, Lang lang, Context context) {
        RDFParser.create()
            .source(in)
            .resolver(IRIxResolverUtils.newIRIxResolverAsGiven())
            // Disabling checking does not seem to give a significant performance gain
            // For a 3GB Trig file parsing took ~1:45 min +- 5 seconds either way
            //.checking(false)
            .base(baseUri)
            .lang(lang)
            .context(context)
            .errorHandler(dftErrorHandler())
            .labelToNode(createLabelToNodeAsGivenOrRandom())
            //.errorHandler(handler)
            .parse(destination);
    }

    public static Flowable<Quad> createFlowableQuads(String filenameOrURI, Lang lang, String baseIRI) {
        return createFlowableQuads(() -> RDFDataMgr.open(filenameOrURI), lang, baseIRI);
    }


    public static Flowable<Quad> createFlowableQuads(Callable<InputStream> inSupplier, Lang lang, String baseIRI) {
        return FlowableEx.fromIteratorSupplier(() -> AsyncParser.of(inSupplier.call(), lang, baseIRI)
                .mutateSources(source -> applyParserDefaults(source))
                .asyncParseQuads(), IteratorCloseable::close)
            // Ensure that the graph node is always non-null
            // Trig parser in Jena 3.14.0 creates quads with null graph
            .map(q -> q.getGraph() != null ? q : Quad.create(Quad.defaultGraphNodeGenerated, q.asTriple()));
    }
//        return createFlowableFromInputStream(
//                    inSupplier,
//                    (th, eh, rawIn) -> (in -> createIteratorQuads(in, lang, baseIRI, eh, th)))
//                // Ensure that the graph node is always non-null
//                // Trig parser in Jena 3.14.0 creates quads with null graph
//                .map(q -> q.getGraph() != null
//                    ? q
//                    : Quad.create(Quad.defaultGraphNodeGenerated, q.asTriple()));
//    }


    /**
     * Creates resources by grouping consecutive quads with the same graph into a Model,
     * and then returning a resource for that graph IRI.
     *
     * {@code
     * GRAPH :s {
     *   :s :p :o .
     *   :o :x :y
     * }
     * }
     *
     *
     * @param inSupplier
     * @param lang
     * @param baseIRI
     * @return
     */
    public static Flowable<Resource> createFlowableResources(Callable<InputStream> inSupplier, Lang lang, String baseIRI) {
        Flowable<Resource> result = createFlowableQuads(inSupplier, lang, baseIRI)
//            .compose(Transformers.<Quad>toListWhile(
//                    (list, t) -> list.isEmpty()
//                                 || list.get(0).getGraph().equals(t.getGraph())))
            .compose(FlowOfQuadsOps.groupToList())
            .map(Entry::getValue)
            .map(list -> list.stream().map(StreamUtils::decodeDistinguished)
            .collect(Collectors.toList()))
            .map(QuadPatternUtils::createResourceFromQuads);

        return result;
    }



    /**
     * Groups consecutive quads with the same graph yeld by createFlowableQuads into datasets
     *
     * @param inSupplier
     * @param lang
     * @param baseIRI
     * @return
     */
    public static Flowable<Dataset> createFlowableDatasets(Callable<InputStream> inSupplier, Lang lang, String baseIRI) {
        Flowable<Dataset> result = createFlowableQuads(inSupplier, lang, baseIRI)
            .compose(FlowOfQuadsOps.datasetsFromConsecutiveQuads(
                    Quad::getGraph,
                    DatasetGraphFactoryEx::createInsertOrderPreservingDatasetGraph))
            ;

        return result;
    }

    public static Flowable<Dataset> createFlowableDatasets(Callable<TypedInputStream> inSupplier) {

//        Flowable<Dataset> result = createFlowableFromInputStream(
//                inSupplier,
//                th -> eh -> in -> createIteratorQuads(
//                        in,
//                        RDFLanguages.contentTypeToLang(in.getContentType()),
//                        in.getBaseURI(),
//                        eh,
//                        th))
        Flowable<Dataset> result = createFlowableQuads(inSupplier)
                .compose(FlowOfQuadsOps.datasetsFromConsecutiveQuads(
                        Quad::getGraph,
                        DatasetGraphFactoryEx::createInsertOrderPreservingDatasetGraph))
                ;
//        .compose(Transformers.<Quad>toListWhile(
//                (list, t) -> list.isEmpty()
//                             || list.get(0).getGraph().equals(t.getGraph())))
//        .compose(DatasetGraphOpsRx.groupToList())
//        .map(Entry::getValue)
//        .map(DatasetFactoryEx::createInsertOrderPreservingDataset);

        return result;
    }

    public static Flowable<Quad> createFlowableQuads(Path path, Iterable<Lang> probeLangs) {
        return createFlowableQuads(() -> RDFDataMgrEx.open(path, probeLangs));
    }

    public static Flowable<Quad> createFlowableQuads(Callable<TypedInputStream> inSupplier) {
        Flowable<Quad> result = FlowableEx.fromIteratorSupplier(() -> {
            TypedInputStream tis = inSupplier.call();
            Lang lang = RDFLanguages.contentTypeToLang(tis.getContentType());
            String base = tis.getBaseURI();
            return AsyncParser.asyncParseQuads(tis.getInputStream(), lang, base);
        }, IteratorCloseable::close);
        return result;
    }

    public static Flowable<Quad> createFlowableTriples(Path path, Iterable<Lang> probeLangs) {
        return createFlowableQuads(() -> RDFDataMgrEx.open(path, probeLangs));
    }

    public static Flowable<Triple> createFlowableTriples(Callable<TypedInputStream> inSupplier) {
        Flowable<Triple> result = FlowableEx.fromIteratorSupplier(() -> {
            TypedInputStream tis = inSupplier.call();
            Lang lang = RDFLanguages.contentTypeToLang(tis.getContentType());
            String base = tis.getBaseURI();
            return AsyncParser.asyncParseTriples(tis.getInputStream(), lang, base);
        }, IteratorCloseable::close);
        return result;
    }

    public static void writeResources(Flowable<? extends Resource> flowable, Path file, RDFFormat format) throws Exception {
        writeDatasets(flowable.map(DatasetUtils::createFromResource), file, format);
    }

    public static void writeResources(Flowable<? extends Resource> flowable, OutputStream out, RDFFormat format) throws Exception {
        writeDatasets(flowable.map(DatasetUtils::createFromResource), out, format);
    }

    // A better approach would be to transform a flowable to write to a file as a side effect
    // Upon flowable completion, copy the file to its final location
    public static void writeDatasets(Flowable<Dataset> flowable, Path file, RDFFormat format) throws Exception {
        try(OutputStream out = new FileOutputStream(file.toFile())) {
            writeDatasets(flowable, out, format);
        }
    }

    public static FlowableTransformer<? super Dataset, Throwable> createWriterDataset(OutputStream out, RDFFormat format) {
        return upstream -> upstream
            .buffer(1)
            .compose(RDFDataMgrRx.createBatchWriterDataset(out, format));
    }

    public static <C extends Collection<? extends Dataset>> FlowableTransformer<C, Throwable> createBatchWriterDataset(OutputStream out, RDFFormat format) {
        QuadEncoderDistinguish encoder = new QuadEncoderDistinguish();
        Lang lang = format.getLang();
        boolean isLangTriples = RDFLanguages.isTriples(lang);

        // TODO Prevent emitting of redundant prefix mappings

        return upstream -> upstream
                .concatMapMaybe(batch -> {
                    for(Dataset item : batch) {
                        Dataset encoded = encoder.encode(item);

                        if(isLangTriples) {
                            Iterator<String> it = item.listNames();
                            while(it.hasNext()) {
                                String name = it.next();
                                Model m = item.getNamedModel(name);
                                RDFDataMgr.write(out, m, format);
                            }
                        } else {
                            RDFDataMgr.write(out, encoded, format);
                        }
                    }
                    out.flush();
                    return Maybe.<Throwable>empty();
                })
                .onErrorReturn(t -> t);
    }


    /**
     *
     * @param <C>
     * @param out
     * @param format Only NQuads is currently supported
     * @return
     */
    public static <C extends Collection<Quad>> FlowableTransformer<C, Throwable> createBatchWriterQuads(OutputStream out, RDFFormat format) {
        if (!Lang.NQUADS.equals(format.getLang())) {
            throw new IllegalArgumentException("Only nquads based formats are currently supported");
        }

        return upstream -> upstream
                .concatMapMaybe(batch -> {
                    RDFDataMgr.writeQuads(out, batch.iterator());
                    out.flush();
                    return Maybe.<Throwable>empty();
                })
                .onErrorReturn(t -> t);
    }


    public static <C extends Collection<Quad>> FlowableTransformer<C, Throwable> createBatchWriterQuads2(OutputStream out, RDFFormat format) {
        if (!Lang.NQUADS.equals(format.getLang())) {
            throw new IllegalArgumentException("Only nquads based formats are currently supported");
        }

        return upstream -> upstream
                .concatMap(Flowable::fromIterable)
                .<StreamRDF>reduceWith(() -> {
                    StreamRDF s = StreamRDFWriterEx.getWriterStream(out, format, null);
                    s.start();
                    return s;
                }, (s, q) -> {
                    s.quad(q);
                    return s;
                })
                .doAfterSuccess(StreamRDF::finish)
                .mapOptional(x -> Optional.<Throwable>empty())
                .onErrorReturn(t -> t)
                .toFlowable();
    }

    public static void writeQuads(Flowable<Quad> flowable, OutputStream out, RDFFormat format) throws IOException {

        Flowable<Throwable> tmp = flowable
            .buffer(128)
            .compose(RDFDataMgrRx.createBatchWriterQuads2(out, format));

        Throwable e = tmp.singleElement().blockingGet();
        if(e != null) {
            throw new IOException(e);
        }
    }

    /**
     *  Does not close the output stream
     *
     *  Note that you can use .createDatasetBatchWriter()
     *  ....blockingForEach(createDatasetBatchWriter())
     *
     *  This does not break the chain and gives freedom over the choice of forEach type (non-/blocking)
     */
    public static void writeDatasets(Flowable<Dataset> flowable, OutputStream out, RDFFormat format) throws IOException {

      Flowable<Throwable> tmp = flowable
          .buffer(1)
          .compose(RDFDataMgrRx.createBatchWriterDataset(out, format));

      Throwable e = tmp.singleElement().blockingGet();
      if(e != null) {
          throw new IOException(e);
      }

if(false) {
        QuadEncoderDistinguish encoder = new QuadEncoderDistinguish();
        flowable
        // Flush every n datasets
        .buffer(1)
        .forEach(items -> {
            for(Dataset item : items) {
                Dataset encoded = encoder.encode(item);
                RDFDataMgr.write(out, encoded, format);
            }
            out.flush();
        });
}


    }

}




//public static RDFIterator<Quad> createIteratorQuads(
//      InputStream in,
//      Lang lang,
//      String baseIRI,
//      UncaughtExceptionHandler eh,
//      Consumer<Thread> th) {
//  AsyncParser.of(in, lang, baseIRI).asyncParseQuads();
//  return createIteratorQuads(
//          in,
//          lang,//RDFLanguages.contentTypeToLang(in.getContentType()),
//          baseIRI,
//          PipedRDFIterator.DEFAULT_BUFFER_SIZE,
//          false,
//          PipedRDFIterator.DEFAULT_POLL_TIMEOUT,
//          Integer.MAX_VALUE,
//          eh,
//          th);
//}

//public static RDFIterator<Quad> createIteratorQuads(
//      TypedInputStream in,
//      UncaughtExceptionHandler eh,
//      Consumer<Thread> th) {
//  return createIteratorQuads(
//          in,
//          RDFLanguages.contentTypeToLang(in.getContentType()),
//          in.getBaseURI(),
//          eh,
//          th);
//}

//public static RDFIterator<Triple> createIteratorTriples(
//      InputStream in,
//      Lang lang,
//      String baseIRI,
//      UncaughtExceptionHandler eh,
//      Consumer<Thread> threadHandler) {
//  return createIteratorTriples(
//          in,
//          lang,//RDFLanguages.contentTypeToLang(in.getContentType()),
//          baseIRI,
//          PipedRDFIterator.DEFAULT_BUFFER_SIZE,
//          false,
//          PipedRDFIterator.DEFAULT_POLL_TIMEOUT,
//          Integer.MAX_VALUE,
//          threadHandler,
//          eh);
//}

//public static RDFIterator<Triple> createIteratorTriples(
//      TypedInputStream in,
//      UncaughtExceptionHandler eh,
//      Consumer<Thread> th) {
//  return createIteratorTriples(
//          in,
//          RDFLanguages.contentTypeToLang(in.getContentType()),
//          in.getBaseURI(),
//          eh,
//          th);
//}


/**
* Adaption from RDFDataMgr.createIteratorQuads that waits for
* data on the input stream indefinitely and allows for thread handling
*
* Creates an iterator over parsing of quads
* Upgrades triples to quads with graph set to Quad.defaultGraphNodeGenerated if lang refers to a triple language
*
* @param input Input Stream
* @param lang Language
* @param baseIRI Base IRI
* @return Iterator over the quads
*/
//public static RDFIterator<Quad> createIteratorQuads(
//      InputStream input,
//      Lang lang,
//      String baseIRI,
//      int bufferSize, boolean fair, int pollTimeout, int maxPolls,
//      UncaughtExceptionHandler eh,
//      Consumer<Thread> th) {
//
//  // Special case N-Quads, because the RIOT reader has a pull interface
//  if ( RDFLanguages.sameLang(RDFLanguages.NQUADS, lang) ) {
//      return new RDFIteratorFromIterator<Quad>(Iter.onCloseIO(
//          RiotParsers.createIteratorNQuads(input, null, RDFDataMgrRx.dftProfile()),
//          input), baseIRI);
//  }
//  // Otherwise, we have to spin up a thread to deal with it
//  RDFIteratorFromPipedRDFIterator<Quad> it = new RDFIteratorFromPipedRDFIterator<>(bufferSize, fair, pollTimeout, maxPolls);
//
//  // Upgrade triples to quads; this happens if quads are requested from a triple lang
//  PipedQuadsStream out = new PipedQuadsStream(it) {
//      @Override
//      public void triple(Triple triple) {
//          Quad q = new Quad(Quad.defaultGraphNodeGenerated, triple);
//          quad(q);
//      }
//  };
//
//  // We need to handle finish ourself in order to pass any raised exception to rxjava
//  StreamRDF ignoreFinishWrapper = new StreamRDFWrapper(out) {
//      @Override public void finish() {}
//  };
//
//
//  Thread t = new Thread(() -> {
//      try {
//          // Invoke start on the sink so that the consumer knows the producer thread
//          // It appears otherwise the producer thread can get interrupted before
//          // the consumer gets to know the producer (which happens on start)
//          out.start();
//          parseFromInputStream(ignoreFinishWrapper, input, baseIRI, lang, null);
//      } catch(Exception e) {
//          // Ensure the exception handler is run before any
//          // thread.join() waiting for this thread
//          eh.uncaughtException(Thread.currentThread(), e);
//      } finally {
//          try {
//              out.finish();
//          } catch (Exception e2) {
//              // Silently ignore failure on finish due to closed consumer
//          }
//      }
//  });
//  th.accept(t);
//  t.start();
//  return it;
//}



//static class State {
//	Path targetPath;
//	Path tmpPath;
//	OutputStream out;
//}
//interface Sink {
//
//}
//
//public static <T> FlowableTransformer<T, T> cache(Path file, BiConsumer<T, OutputStream> serializer) {
//
//	State[] state = {null};
//	return f -> f
//			.doOnSubscribe(s -> state[0] = new State())
//			.doOnNext(item -> serializer.accept(state[0].out, item))
//			.doOnCancel(onCancel)
//		;
//
//
////	return f ->
////      f.doOnSubscrible // open the stream
////		f.doOnNext() // write the item
////	    f.onCancel() // delete the file
////      f.onError // delete the file
////	    f.onComplete // close the stream, copy the file to the final location
//
////        final BiPredicate<? super List<T>, ? super T> condition, boolean emitRemainder) {
////    return collectWhile(ListFactoryHolder.<T>factory(), ListFactoryHolder.<T>add(), condition, emitRemainder);
//}


//public Consumer<? extends Dataset> createWriter(OutputStream out, RDFFormat format, int flushSize) {
//
//}

//public static Consumer<Dataset> createDatasetWriter(OutputStream out, RDFFormat format) {
//    return ds -> RDFDataMgr.write(out, ds, format);
//}


//public static <T, I extends InputStream> Flowable<T> createFlowableFromInputStream(
//      Callable<I> inSupplier,
//      IteratorFactoryFactory<I, T> iff) {
//
//  // In case the creation of the iterator from an inputstream involves a thread
//  // perform setup of the exception handler
//
//  // If there is a thread, we join on it before completing the flowable in order to
//  // capture any possible error
//
//  Flowable<T> result = Flowable.generate(
//          () -> {
//              FlowState<T> state = new FlowState<>();
//              I rawIn = inSupplier.call();
//              // Closing the flowable may interrupt threads which may cause
//              // unwanted close of the underlying input stream
//              // state.in = Channels.newInputStream(new ReadableByteChannelWithoutCloseOnInterrupt(rawIn));
//
//              state.setIn(rawIn);
//              state.setIterator(iff.apply(state::setProducerThread, state::handleProducerException, rawIn).apply(state.in));
//
//
//              // state.reader = fn.apply(state::setProducerThread).apply(state::handleException).apply(state.in);
//
//              return state;
//          },
//          (state, emitter) -> {
//              try {
//                  boolean hasNext;
//                  boolean isCancelled = false;
//
//                  try {
//                      hasNext = state.iterator.hasNext();
//                  } catch (CancellationException | RiotException e) {
//                      // RiotException is assumed to be "Producer dead"
//                      hasNext = false;
//                      isCancelled = true;
//                  }
//
////					System.out.println("Generator invoked");
//                  if (hasNext && !state.closeInvoked) {
////						System.out.println("hasNext = true");
//                      T item = state.iterator.next();
//                      emitter.onNext(item);
//                  } else {
////						System.out.println("hasNext = false; Waiting for any pending exceptions from producer thread");
//                      if (state.producerThread != null && !isCancelled) {
//                          state.producerThread.join();
//                      }
////						System.out.println("End");
//
//                      Throwable t = state.raisedException;
//                      boolean report = true;
//                      if (t != null) {
//                          boolean isParseError = t instanceof RiotParseException;
//
//                          // Parse errors after an invocation of close are ignored
//                          // I.e. if we asked for 5 items, and there is parse error at the 6th one,
//                          // we still complete the original request without errors
//                          if (isParseError && state.closeInvoked) {
//                              report = false;
//                          }
//                      }
//
//                      if (t != null && report) {
//                          emitter.onError(state.raisedException);
//                      } else {
//                          emitter.onComplete();
//                      }
//                  }
//              } catch(Exception e) {
//                  emitter.onError(e);
//              }
//          },
//          state -> state.close());
//  return result;
//}
