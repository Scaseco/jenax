package org.aksw.jenax.arq.service.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Supplier;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.commons.io.binseach.BinarySearcher;
import org.aksw.commons.io.hadoop.binseach.bz2.BlockSources;
import org.aksw.commons.io.hadoop.binseach.v2.BinSearchResourceCache;
import org.aksw.commons.io.hadoop.binseach.v2.BinarySearchBuilder;
import org.aksw.commons.util.entity.EntityInfo;
import org.aksw.commons.util.stream.CollapseRunsSpec;
import org.aksw.commons.util.stream.StreamOperatorCollapseRuns;
import org.aksw.jena_sparql_api.io.binseach.GraphFromPrefixMatcher;
import org.aksw.jena_sparql_api.io.binseach.GraphFromSubjectCache;
import org.aksw.jena_sparql_api.io.binseach.StageGeneratorGraphFindRaw;
import org.aksw.jenax.arq.util.binding.QueryIterOverQueryExec;
import org.aksw.jenax.arq.util.exec.query.QueryExecUtils;
import org.aksw.jenax.arq.util.lang.RDFLanguagesEx;
import org.aksw.jenax.sparql.query.rx.RDFDataMgrEx;
import org.apache.hadoop.io.compress.BZip2Codec;
import org.apache.hadoop.io.compress.SplittableCompressionCodec;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.AsyncParser;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.Rename;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterCommonParent;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

/**
 * TODO Factory out into a more general class that delegates each bindings to custom processor
 *
 * @author Claus Stadler, Dec 5, 2018
 *
 */
public class ServiceExecutorFactoryVfsUtils {

    public static final String XBINSEARCH = "x-binsearch:";
    public static final String XFSRDFSTORE = "x-fsrdfstore:";
    public static final String FILE = "file:";
    public static final String VFS = "vfs:";

    // public static Supplier<SplittableCompressionCodec> CODEC_FACTORY_BZIP2 = () -> new BZip2CodecAdapted(128);
    public static Supplier<SplittableCompressionCodec> CODEC_FACTORY_BZIP2 = () -> new BZip2Codec();


    protected static Logger logger = LoggerFactory.getLogger(ServiceExecutorFactoryVfsUtils.class);


    public static Path toPath(Node node) {
        Entry<Path, Map<String, String>> tmp = toPathSpec(node);
        Path result = tmp.getKey();
        return result;
    }

    public static Entry<Path, Map<String, String>> toPathSpec(Node node) {
        Entry<Path, Map<String, String>> result;
        if (node.isURI()) {
            result = toPathSpec(node.getURI());
        } else {
            result = null;
        }

        return result;
    }

    public static Entry<Path, Map<String, String>> toPathSpec(String uriStr) {
        Entry<Path, Map<String, String>> result = null;
        try {
            String tmp = uriStr;
            if (tmp.startsWith(XBINSEARCH)) {
                tmp = tmp.substring(XBINSEARCH.length());

                result = toPathSpecRaw(tmp);

                if (result != null) {
                    result.getValue().put("binsearch", "true");
                }
            } else {
                result = toPathSpecRaw(uriStr);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public static Entry<Path, Map<String, String>> toPathSpecRaw(String tmp) throws URISyntaxException, IOException {
        Path path = null;
        Map<String, String> params = new LinkedHashMap<>();

        boolean useVfs = false;
        boolean useFile = false;

        if (tmp.startsWith(VFS)) {
            useVfs = true;
            tmp = tmp.substring(VFS.length());

            // In the case of vfs replace http (without trailing number) with e.g. http4
            // in order to use the same http client as jena does
            // Also, my fix for VFS-805 does not work with the vfs 2.8.0's default http client 3

            tmp = tmp.replaceAll("^http(?!\\d+)", "http4");

        } else if (tmp.startsWith(FILE)) {
            useFile = true;
        } else {
            tmp = null;
        }

        if (tmp != null) {
            URI uri = new URI(tmp);
            params = createMapFromUriQueryString(uri);

            // Cut off any query string
            URI effectiveUri = new URI(tmp.replaceAll("\\?.*", ""));

            if (useVfs) {
                String fileSystemUrl = effectiveUri.getScheme() + "://" + effectiveUri.getAuthority();

                URI fileSystemUri = URI.create("vfs:" + fileSystemUrl);

                // Get-or-create file system
                FileSystem fs;
                try {
                    fs = FileSystems.getFileSystem(fileSystemUri);
                } catch (FileSystemNotFoundException e1) {
                    try {
                        Map<String, Object> env = null; // new HashMap<>();
                        fs = FileSystems.newFileSystem(
                            fileSystemUri,
                            env);
                    } catch (FileSystemAlreadyExistsException e2) {
                        // There may have been a concurrent registration of the file system
                        fs = FileSystems.getFileSystem(fileSystemUri);
                    }
                }

                String pathStr = effectiveUri.getPath();
                Path root = IterableUtils.expectOneItem(fs.getRootDirectories());
                path = root.resolve(pathStr);
            } else if (useFile) {
                path = Paths.get(uri);
            }
        }

        Entry<Path, Map<String, String>> result =
                path == null ? null : Maps.immutableEntry(path, params);

        return result;
    }


    /**
     * Only retains first value
     * @return
     */
    public static Map<String, String> createMapFromUriQueryString(URI uri) {
        return createMapFromUriQueryString(uri, new LinkedHashMap<>());
    }

    /**
     * Only retains first value
     * @return
     */
    public static Map<String, String> createMapFromUriQueryString(URI uri, Map<String, String> result) {
        List<NameValuePair> pairs = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);
        for (NameValuePair pair : pairs) {
            result.putIfAbsent(pair.getName(), pair.getValue());
        }

        return result;
    }



//
//    public static Entry<Path, Map<String, String>> toPathSpec(Node node) {
//        Entry<Path, Map<String, String>> result = null;
//        if(node.isURI()) {
//            String uriStr = node.getURI();
//
//            boolean isFileRef = uriStr.startsWith(FILE);
//            if(isFileRef) {
//                Path path;
//                try {
//                    URI uri = new URI(uriStr);
//                    Map<String, String> params = UriUtils.createMapFromUriQueryString(uri);
//
//                    // Cut off any query string
//                    URI effectiveUri = new URI(uriStr.replaceAll("\\?.*", ""));
//
//                    path = Paths.get(effectiveUri);
////                    boolean fileExists = Files.exists(path);
//
//                    // result = fileExists ? Maps.immutableEntry(path, params) : null;
//                    return Maps.immutableEntry(path, params);
//                } catch (URISyntaxException e) {
//                    //throw new RuntimeException(e);
//                    // Nothing todo; we simply return null if we fail
//                }
//            }
//        }
//
//        return result;
//    }


//    @Override
//    protected QueryIterator nextStage(Binding outerBinding)
//    {
//        OpService op = (OpService)QC.substitute(opService, outerBinding);
//        Node serviceNode = op.getService();
//
//        //Path path = toPath(serviceNode);
//        Entry<Path, Map<String, String>> fileSpec = toPathSpec(serviceNode);
//
//        QueryIterator result = fileSpec == null
//                ? super.nextStage(outerBinding)//nextStageService(outerBinding)
//                : nextStagePath(outerBinding, fileSpec.getKey(), fileSpec.getValue());
//
//        return result;
//    }

    /** Context can provide a BinSearchResourceCache */
    public static Graph createGraphBinSearch(Path path, Context context) throws IOException {
        EntityInfo info;
        try (InputStream in = Files.newInputStream(path)) {
            info = RDFDataMgrEx.probeEntityInfo(in, Collections.singleton(Lang.NTRIPLES));
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }

        boolean isNtriples = Objects.equals(RDFLanguagesEx.findLang(info.getContentType()), Lang.NTRIPLES);

        if (!isNtriples) {
            throw new RuntimeException("No ntriples content in " + path);
        }

        boolean isBzip2 = Collections.singletonList("bzip2").equals(info.getContentEncodings());

        // On dnb-all_lds_20200213.sorted.nt.bz2:
//      int bufferSize = 4 * 1024; // 363 requests
//      int bufferSize = 8 * 1024; // 187 requests
//      int bufferSize = 16 * 1024; // 98 requests
//      int bufferSize = 32 * 1024; // 54 requests
//      int bufferSize = 64 * 1024; // 33 requests
        int bufferSize = 128 * 1024; // 22 requests
//      int bufferSize = 256 * 1024; // 16 requests
//      int bufferSize = 512 * 1024; // 14 requests

        Context cxt = context.copy();
        BinSearchResourceCache resourceCache = ServiceExecutorBinSearch.getOrCreate(cxt);

        BinarySearchBuilder binSearchBuilder =  BinarySearchBuilder.newBuilder()
                .setSource(path)
                .setResourceCache(resourceCache);

        boolean useV1 = false;
        BinarySearcher binarySearcher;
        binarySearcher = isBzip2
            ? binSearchBuilder.setCodec(CODEC_FACTORY_BZIP2.get()).build()
            : binSearchBuilder.build();

        Graph graph = new GraphFromPrefixMatcher(binarySearcher);
//        // GraphFromSubjectCache subjectCacheGraph = new GraphFromSubjectCache(graph);
//        GraphFromSubjectCache subjectCacheGraph = null;
//        Graph effectiveGraph = graph;
//        Model model = ModelFactory.createModelForGraph(effectiveGraph);
//        // QueryExecution qe = QueryExecutionFactory.create(query, model);
        return graph;
    }


    public static QueryIterator nextStage(OpService opService, Binding outerBinding, ExecutionContext execCxt, Path path, Map<String, String> params) {
        Context context = execCxt.getContext();
        // OpService op = (OpService)QC.substitute(opService, outerBinding);
        boolean silent = opService.getSilent() ;
        QueryIterator qIter = null;
        try {
            Op opRemote = opService.getSubOp();
            Op opRestored = Rename.reverseVarRename(opRemote, true);
            Query query = OpAsQuery.asQuery(opRestored);
            Map<Var, Var> varMapping = QueryExecUtils.computeVarMapping(opRemote, opRestored);

            //Flowable<Binding> bindingFlow = null;
            // Iterator<Binding> itBindings = null;

            boolean specialStreamProcessingApplied = false;

            boolean useBinSearch = params.containsKey("binsearch");
            String binSearchVal = params.getOrDefault("binsearch", "");
            if (useBinSearch || "true".equalsIgnoreCase(binSearchVal)) {
                specialStreamProcessingApplied = true;

                EntityInfo info;
                try (InputStream in = Files.newInputStream(path)) {
                    info = RDFDataMgrEx.probeEntityInfo(in, Collections.singleton(Lang.NTRIPLES));
                } catch (IOException e1) {
                    throw new RuntimeException(e1);
                }

                boolean isNtriples = Objects.equals(RDFLanguagesEx.findLang(info.getContentType()), Lang.NTRIPLES);

                if (!isNtriples) {
                    throw new RuntimeException("No ntriples content in " + path);
                }

                boolean isBzip2 = Collections.singletonList("bzip2").equals(info.getContentEncodings());



                // On dnb-all_lds_20200213.sorted.nt.bz2:
//              int bufferSize = 4 * 1024; // 363 requests
//              int bufferSize = 8 * 1024; // 187 requests
//              int bufferSize = 16 * 1024; // 98 requests
//              int bufferSize = 32 * 1024; // 54 requests
//              int bufferSize = 64 * 1024; // 33 requests
                int bufferSize = 128 * 1024; // 22 requests
//              int bufferSize = 256 * 1024; // 16 requests
//              int bufferSize = 512 * 1024; // 14 requests

                Context cxt = context.copy();
                BinSearchResourceCache resourceCache = ServiceExecutorBinSearch.getOrCreate(cxt);

                BinarySearchBuilder binSearchBuilder =  BinarySearchBuilder.newBuilder()
                        .setSource(path)
                        .setResourceCache(resourceCache);

                // Model generation wrapped as a flowable for resource management
                // bindingFlow = Flowable.generate(() -> {
                boolean useV1 = false;
                BinarySearcher binarySearcher;
                if (useV1) {
                    binarySearcher = isBzip2
                        ? BlockSources.createBinarySearcherBz2(path, bufferSize)
                        : BlockSources.createBinarySearcherText(path, bufferSize);
                } else {
                    binarySearcher = isBzip2
                        ? binSearchBuilder.setCodec(CODEC_FACTORY_BZIP2.get()).build()
                        : binSearchBuilder.build();
                }

                Graph graph = new GraphFromPrefixMatcher(binarySearcher);
                // GraphFromSubjectCache subjectCacheGraph = new GraphFromSubjectCache(graph);
                GraphFromSubjectCache subjectCacheGraph = null;
                Graph finalGraph = graph;
                // QueryExecution qe = QueryExecutionFactory.create(query, model);

                cxt.set(ARQ.stageGenerator, new StageGeneratorGraphFindRaw());
                // XXX Even better would be to pass on the cancel symbol set up
                // by QueryExecDataset
                QueryExec qe = QueryExec.graph(finalGraph)
                    .query(query)
                    .context(cxt)
                    .build();

                qIter = new QueryIterOverQueryExec(execCxt, qe);
            }

            // TODO Allow subject-streams to take advantage of binsearch:
            // With SERVICE<...?binsearch=true&stream=s> { ?x ?y ?z }
            // we can optimize joins when subject variables are bound

            String streamVal = params.get("stream");
            if(!Strings.isNullOrEmpty(streamVal)) {
                if("s".equalsIgnoreCase(streamVal)) {
                    specialStreamProcessingApplied = true;

                    // Stream by subject - useful for answering star patterns
                    List<Lang> tripleLangs = RDFLanguagesEx.getTripleLangs();
                    TypedInputStream tmp = RDFDataMgrEx.open(path.toString(), tripleLangs);
                    Lang lang = RDFLanguages.contentTypeToLang(tmp.getContentType());
                    String base = tmp.getBaseURI();
                    IteratorCloseable<Triple> baseIt = AsyncParser.of(tmp, lang, base).asyncParseTriples();

                    // Collapse triples with the same subject into graphs.
                    Iterator<Entry<Node, Graph>> graphIt = StreamOperatorCollapseRuns
                        .create(CollapseRunsSpec.create(Triple::getSubject, GraphFactory::createDefaultGraph, Graph::add))
                        .transform(baseIt);
                    graphIt = Iter.onClose(graphIt, baseIt::close);

                    Iterator<Binding> bindingIt = Iter.flatMap(graphIt, e -> {
                            Graph g = e.getValue();
                            QueryExec qe2 = QueryExec.graph(g).query(query.cloneQuery()).build();
                            QueryIterator qIter2 = new QueryIterOverQueryExec(execCxt, qe2);
                            return qIter2;
                        });

                    qIter = QueryIterPlainWrapper.create(bindingIt, execCxt);

//                    bindingFlow = RDFDataMgrRx.createFlowableTriples(() -> tmp)
//                            .compose(GraphOpsRx.graphFromConsecutiveTriples(Triple::getSubject, GraphFactory::createDefaultGraph))
//                            .map(ModelFactory::createModelForGraph)
//                            //.parallel()
//                            .flatMap(m ->
//                                SparqlRx.execSelectRaw(() -> QueryExecutionFactory.create(query.cloneQuery(), m)));
//                            //.sequential();

                    // itBindings = flow.blockingIterable().iterator();
                } else {
                    throw new RuntimeException("For streaming in SERVICE, only 's' for subjects is presently supported.");
                }
            }

            if (!specialStreamProcessingApplied) {
                DatasetGraph dataset = DatasetGraphFactory.create();
                try (InputStream in = RDFDataMgrEx.probeEncodings(Files.newInputStream(path), null)) {
                    TypedInputStream tis = RDFDataMgrEx.probeLang(in, RDFDataMgrEx.DEFAULT_PROBE_LANGS);

                    // String url = path.toUri().toString();
                    Lang lang = RDFLanguages.contentTypeToLang(tis.getContentType());
                    RDFDataMgr.read(dataset, tis.getInputStream(), lang);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

//    	    	// TODO Probably add namespaces declared on query scope (how to access them?)
                //query.addGraphURI(path.toUri().toString());

//                qe = QueryExecutionFactory.create(query, dataset);//, input);
//                right = new QueryIteratorResultSet(qe.execSelect());
                // bindingFlow = SparqlRx.execSelectRaw(() -> QueryExecutionFactory.create(query, dataset));
                        // .blockingIterable().iterator();
                Context cxt = context.copy();
                QueryExec qe = QueryExec.dataset(dataset)
                        .query(query)
                        .context(cxt)
                        .build();

                qIter = new QueryIterOverQueryExec(execCxt, qe);
            }


            // In silent mode we consume all data into a data bag
            // so that any exception raised during iteration gets caught here
            if (silent && specialStreamProcessingApplied) {
                qIter = new QueryIteratorMaterialize(qIter, execCxt);
            }

//            Iterator<Binding> tmp = bindingFlow.blockingIterable().iterator();
//            QueryIterator right = new QueryIterPlainWrapper(tmp) {
//                @Override
//                protected void requestCancel() {
//                    ((Disposable)tmp).dispose();
//                    super.requestCancel();
//                }
//
//                @Override
//                protected void closeIterator() {
//                    ((Disposable)tmp).dispose();
//                    super.closeIterator();
//                }
//            };


            // This iterator is materialized already otherwise we may end up
            // not servicing the HTTP connection as needed.
            // In extremis, can cause a deadlock when SERVICE loops back to this server.
            // Add tracking.
            // qIter = QueryIter.makeTracked(right, getExecContext()) ;
            // qIter = right;

            if (varMapping != null) {
                qIter = QueryIter.map(qIter, varMapping);
            }

        } catch (Exception ex) {
            if ( silent )
            {
                logger.warn("SERVICE <" + opService.getService().toString() + ">: " + ex.getMessage()) ;
                // Return the input
                return QueryIterSingleton.create(outerBinding, execCxt) ;
            }
            throw new RuntimeException(ex);
        }

        // Need to put the outerBinding as parent to every binding of the service call.
        // There should be no variables in common because of the OpSubstitute.substitute
        QueryIterator qIter2 = new QueryIterCommonParent(qIter, outerBinding, execCxt) ;

        return qIter2 ;
    }
}
