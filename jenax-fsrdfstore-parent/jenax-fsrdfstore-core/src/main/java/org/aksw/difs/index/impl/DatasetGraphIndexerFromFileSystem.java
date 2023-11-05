package org.aksw.difs.index.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.io.util.PathUtils;
import org.aksw.commons.io.util.SymLinkUtils;
import org.aksw.commons.io.util.UriToPathUtils;
import org.aksw.commons.io.util.symlink.SymbolicLinkStrategy;
import org.aksw.commons.path.core.PathStr;
import org.aksw.commons.txn.api.Txn;
import org.aksw.commons.txn.api.TxnResourceApi;
import org.aksw.commons.txn.impl.FileSyncImpl;
import org.aksw.commons.txn.impl.FileUtilsExtra;
import org.aksw.commons.txn.impl.ResourceRepository;
import org.aksw.commons.util.array.Array;
import org.aksw.commons.util.string.StringUtils;
import org.aksw.difs.index.api.DatasetGraphIndexPlugin;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.NodeUtils;
import org.apache.jena.sparql.util.Symbol;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.io.MoreFiles;


/**
 * Indexer based on predicates. Indexes objects of triples for the given predicate.
 *
 * TODO Add a flag whether to index subjects instead.
 *
 * @author raven
 *
 */
public class DatasetGraphIndexerFromFileSystem
    implements DatasetGraphIndexPlugin
{
    protected SymbolicLinkStrategy symlinkStrategy;

    protected Path basePath;
//    protected Path propertyFolder;
    protected Node propertyNode;
    protected Function<? super Node, String[]> objectToPath;
    protected Function<String, Path> uriToPath;

    /** The file the index point to - TODO this should be obtained from some store object */
    protected String tgtFilename = "data.trig";

    // We need that graph in order to re-use its mapping
    // from (subject) iris to paths
    // protected DatasetGraphFromFileSystem syncedGraph;
    protected ResourceRepository<String> syncedGraph;


    protected Symbol indexerKey;

    // Extract the symlink strategy from the dataset graph
//    public static SymlinkStrategy extractSymlinkStrategy(DatasetGraph dg) {
//    	DatasetGraphFromTxnMgr tdg = (DatasetGraphFromTxnMgr)dg;
//    	TxnMgr txnMgr = tdg.getTxnMgr();
//    	SymlinkStrategy result = txnMgr.getSymlinkStrategy();
//    	return result;
//    }

    public DatasetGraphIndexerFromFileSystem(
            SymbolicLinkStrategy symlinkStrategy,
            ResourceRepository<String> syncedGraph,
            Node propertyNode,
            Path basePath,
            Function<Node, String[]> objectToPath) {
        super();
        this.symlinkStrategy = symlinkStrategy;
        this.basePath = basePath;
        this.propertyNode = propertyNode;
        this.syncedGraph = syncedGraph;
        this.objectToPath = objectToPath;

        this.indexerKey = Symbol.create("indexer:" + this.getClass() + ":" + propertyNode);

    }

    public static String[] uriNodeToPath(Node node) {
        String[] result = node.isURI()
                ? UriToPathUtils.toPathSegments(node.getURI())
                : null;

        return result;
    }


    public static String[] iriOrLexicalFormToPath(Node node) {
        String[] result = node.isLiteral()
                ? new String[] {StringUtils.urlEncode(node.getLiteralLexicalForm())}
                : node.isURI()
                    ? UriToPathUtils.toPathSegments(node.getURI())
                    : node.isBlank()
                        ? PathUtils.splitBySlash(node.getBlankNodeLabel())
                        : null;
        return result;
    }

    public static Path mavenStringToPath(Node node) {
        String str = node.isURI() ? node.getURI() : NodeUtils.stringLiteral(node);

        // Remove trailing slashes and
        // FIXME Replace '..' only if it would be interpreted as moving up the directory
        Path result = null;
        if (str != null) {
            String[] parts = str.replace(':', '/').replaceAll("^/*", "").split("/");
            String tmp = Arrays.asList(parts).stream()
                .filter(part -> !part.equals(".."))
                .map(StringUtils::urlEncode)
                .collect(Collectors.joining("/"));

            result = Paths.get(tmp);
        }

        return result;
    }


    public static String pathToFilename(String[] relPath) {
        String result = PathUtils.join(relPath).replace("/", ".");
        if (result.length() > 64) {
            result = StringUtils.md5Hash(result);
        }
        return result;
    }


//    public Entry<Path, Path> computeLink(DatasetGraph dg, Node g, Node s, Node p, Node o) {
//    	Entry<Path, Path> result = null;
//
//        if (evaluateFind(s, p, o) != null) {
//            String[] idxRelPath = objectToPath.apply(o);
//
//
////            Path idxRelPath = UriToPathUtils.resolvePath(oRelPath);
//            Path idxFullPath = PathUtils.resolve(basePath, idxRelPath);
//
//            String tgtIri = g.getURI();
//            Path tgtBasePath = syncedGraph.getRootPath();
//            String[] tgtRelPath = syncedGraph.getPathSegments(tgtIri);
//
//            String coreName = pathToFilename(tgtRelPath);
//
//            // String tgtFileName = syncedGraph.getFilename();
//            // String tgtFileName = filename;
//            Path symLinkTgtAbsFile = PathUtils.resolve(tgtBasePath, tgtRelPath).resolve(tgtFilename);
////            Path symLinkTgtRelFile = idxFullPath.relativize(symLinkTgtAbsFile);
//
//            Path file = Paths.get(tgtFilename);
//            String prefix = MoreFiles.getNameWithoutExtension(file);
//            prefix = prefix + "." + coreName;
//            String suffix = MoreFiles.getFileExtension(file);
//            suffix = suffix.isEmpty() ? "" : "." + suffix;
//            suffix += ".link";
//        }
//
//        return result;
//    }
//
//    @Override
//    public void add(DatasetGraph dg, Node g, Node s, Node p, Node o) {
//    	Entry<Path, Path> e = computeLink(dg, g, s, p, o);
//    	if (e != null) {
//    		Path path = e.getKey();
//    		Path target = e.getValue();
//    		try {
//	    		FileUtilsX.ensureFolderExists(path, x -> {
//	    			symlinkStrategy.createSymbolicLink(path, target);
//	    		});
//    		} catch (Exception e) {
//    			throw new RuntimeException(e);
//    		}
//    	}
//    }

    @Override
    public void add(Txn txn, DatasetGraph dg, Node g, Node s, Node p, Node o) {
//        Node g = quad.getGraph();
//        Node s = quad.getSubject();
//        Node p = quad.getPredicate();
//        Node o = quad.getObject();
        Float evalResult = evaluateFind(s, p, o);
        if (evalResult != null) {


            String[] idxRelPath = objectToPath.apply(o);

            Quad insertQuad = new Quad(g, s, p, o);


            // The delete operation creates an inverted index in the
            // graph's context
            // Update it if it exists
            Context cxt = dg.getContext();
            Multimap<Array<String>, Quad> invertedIndex = cxt.get(indexerKey);
            if (invertedIndex != null) {
                invertedIndex.put(Array.wrap(idxRelPath), insertQuad);
            }


//            Path idxRelPath = UriToPathUtils.resolvePath(oRelPath);
            Path idxFullPath = PathUtils.resolve(basePath, idxRelPath);

            String tgtIri = g.getURI();
            Path tgtBasePath = syncedGraph.getRootPath();
            String[] tgtRelPath = syncedGraph.getPathSegments(tgtIri);

            String coreName = pathToFilename(tgtRelPath);

            // String tgtFileName = syncedGraph.getFilename();
            // String tgtFileName = filename;
            Path symLinkTgtAbsFile = PathUtils.resolve(tgtBasePath, tgtRelPath).resolve(tgtFilename);
//            Path symLinkTgtRelFile = idxFullPath.relativize(symLinkTgtAbsFile);

            // Compute prefix/suffix
            Path file = Paths.get(tgtFilename);
            String tmpPrefix = MoreFiles.getNameWithoutExtension(file);
            String prefix = tmpPrefix + "." + coreName;

            String tmpSuffix = MoreFiles.getFileExtension(file);
            tmpSuffix = tmpSuffix.isEmpty() ? "" : "." + tmpSuffix;
            String suffix = tmpSuffix + ".link";


            try {
                FileUtilsExtra.ensureFolderExists(idxFullPath, () -> {

                    // TODO We probably want a mgmtLock on the folder to prevent concurrent modification
                    // to the set of links

                    SymLinkUtils.allocateSymbolicLink(symlinkStrategy, symLinkTgtAbsFile, idxFullPath, FileSyncImpl::getBaseName, prefix, suffix,
                            (linkFile, tgt) -> allocateSymLinkInTxn(txn, symlinkStrategy, linkFile, symLinkTgtAbsFile));
                });

                // TODO Possibly extend allocateSymbolicLink with a flag to update the symlink rather
                // having to catch FileAlreadyExistsException here
                //SymlinkStrategy symlinkStrategy = extractSymlinkStrategy(dg);
            } catch (Exception e) {
                 throw new RuntimeException(e);
            }
        }
    }


    public static Path allocateSymLinkInTxn(Txn txn, SymbolicLinkStrategy symlinkStrategy, Path linkFile, Path tgt)
        throws IOException
    {
        Path baseFolder = txn.getTxnMgr().getRootPath();
        String[] indexLinkId = PathUtils.getPathSegments(baseFolder.relativize(linkFile));
        TxnResourceApi txnResApi = txn.getResourceApi(PathStr.newRelativePath(indexLinkId));

        txnResApi.declareAccess();
        txnResApi.lock(true);

        Path r = txnResApi.getFileSync().getNewContentTmpFile();
        Files.createDirectories(r.getParent());

        Path relTgt = r.getParent().relativize(tgt);

        // Files.deleteIfExists(r);
        symlinkStrategy.createSymbolicLink(r, relTgt);

        return r;
    }


    protected Multimap<Array<String>, Quad> createInvertedIndex(DatasetGraph dg) {
        Multimap<Array<String>, Quad> keyToQuads = HashMultimap.create();

        Streams.stream(dg.find(Node.ANY, Node.ANY, propertyNode, Node.ANY))
                .flatMap(q -> {
                    String[] v = objectToPath.apply(q.getObject());
                    return Optional.ofNullable(v)
                        .map(vv -> new SimpleEntry<>(Array.wrap(vv), q))
                        .stream();
                })
                .forEach(e -> keyToQuads.put(e.getKey(), e.getValue()));

        return keyToQuads;
    }


    /**
     * Delete a quad from the index.
     *
     */
    @Override
    public void delete(Txn txn, DatasetGraph dg, Node g, Node s, Node p, Node o) {
//        Node g = quad.getGraph();
//        Node s = quad.getSubject();
//        Node p = quad.getPredicate();
//        Node o = quad.getObject();
        if (evaluateFind(s, p, o) != null) {
//            String idxIri = o.getURI();
//            Path idxRelPath = UriToPathUtils.resolvePath(idxIri);
            String[] idxRelPath = objectToPath.apply(o);

            Quad deleteQuad = new Quad(g, s, p, o);

            // FIXME This is a slow operation - needs caching in general
            // Check whether any other quad in the same graph wrt. dg maps to the same idxRelPath - if so do not delete the symlink
//            long count = Streams.stream(dg.find(g, Node.ANY, p, Node.ANY))
//                .filter(q -> !q.equals(deleteQuad))
//                .filter(q -> {
//                    String[] otherRelPath = objectToPath.apply(q.getObject());
//                    return otherRelPath.equals(idxRelPath);
//                })
//                .count();
//
//            if (count == 0) {

            Context cxt = dg.getContext();
            Multimap<Array<String>, Quad> invertedIndex = cxt.get(indexerKey);
            if (invertedIndex == null) {
                invertedIndex = createInvertedIndex(dg);
                cxt.put(indexerKey, invertedIndex);
            }

            Set<Quad> quadsThatMapToTheSameIndexKey = (Set<Quad>)invertedIndex.get(Array.wrap(idxRelPath));
            quadsThatMapToTheSameIndexKey.remove(deleteQuad);


            if (quadsThatMapToTheSameIndexKey.isEmpty()) {
                Path idxFullPath = PathUtils.resolve(basePath, idxRelPath);

    // Should we sanity check that the symlink refers to the exact same target
    // as it would if we created it from the quad?
    //            String tgtIri = g.getURI();
    //            Path tgtRelPath = syncedGraph.getRelPathForIri(tgtIri);
    //            String tgtFileName = syncedGraph.getFilename();
                String tgtIri = g.getURI();
                Path tgtBasePath = syncedGraph.getRootPath();
                String[] tgtRelPath = syncedGraph.getPathSegments(tgtIri);

                String coreName = pathToFilename(tgtRelPath);


                String[] tmp = PathUtils.getPathSegments(txn.getTxnMgr().getRootPath().relativize(tgtBasePath).normalize());
                tmp = ArrayUtils.addAll(tmp, tgtRelPath);
                tmp = ArrayUtils.add(tmp, "data.trig");
                org.aksw.commons.path.core.Path<String> allSegments = PathStr.newRelativePath(tmp);

                // Compute prefix/suffix
                Path file = Paths.get(tgtFilename);
                String tmpPrefix = MoreFiles.getNameWithoutExtension(file);
                String prefix = tmpPrefix + "." + coreName;

                String tmpSuffix = MoreFiles.getFileExtension(file);
                tmpSuffix = tmpSuffix.isEmpty() ? "" : "." + tmpSuffix;
                String suffix = tmpSuffix + ".link";

//	             String tgtFileName = filename;


//	            Path file = Paths.get(tgtFilename);
//	            String prefix = preprefix + MoreFiles.getNameWithoutExtension(file);
//	            String suffix = MoreFiles.getFileExtension(file);
//	            suffix = suffix.isEmpty() ? "" : "." + suffix;

                // FIXME This looks broken - delete won't work
                // Path symLinkSrcFile = idxFullPath.resolve(tgtFilename);

    //            Path symLinkTgtFile = tgtRelPath.resolve(tgtFileName);


                try {
                    Table<String, Path, Path> links;
                    try {
                        // TODO There might be a .sync.tmp file
                        links = SymLinkUtils.readSymbolicLinks(symlinkStrategy, idxFullPath, FileSyncImpl::getBaseName, prefix, suffix);
                    } catch (NoSuchFileException e) {
                        links = HashBasedTable.create();
                    }
                    // boolean isSymlink = Files.isSymbolicLink(symLinkSrcFile);
                    // if (isSymlink) {
                    {
                        // TODO Find the entry that links to the target
                        Cell<String, Path, Path> deletePathCell = links.cellSet().stream()
                            .filter(srcToTgt -> {
                                Path absTgt = SymLinkUtils.resolveSymLinkAbsolute(srcToTgt.getColumnKey(), srcToTgt.getValue());

                                org.aksw.commons.path.core.Path<String> key = linkTargetToKey(txn, absTgt);
                                boolean r = Objects.equals(key, allSegments);
                                return r;
                            })
                            // .map(Cell::getRow)
                            .findFirst().orElse(null)
                            ;

                        if (deletePathCell != null) {
                            // Construct the path to the target resource (rather than any of the involved temporary files)
                            Path deletePath = deletePathCell.getColumnKey().resolveSibling(deletePathCell.getRowKey());

                            Path baseFolder = txn.getTxnMgr().getRootPath();

                            // TODO Get the base name
                            String[] indexLinkId = PathUtils.getPathSegments(baseFolder.relativize(deletePath).normalize());

                            TxnResourceApi txnResApi = txn.getResourceApi(PathStr.newRelativePath(indexLinkId));
                            txnResApi.declareAccess();
                            txnResApi.lock(true);
                            txnResApi.getFileSync().markForDeletion();

                            // FileUtilsExtra.deleteFileIfExistsAndThenDeleteEmptyFolders(deletePath, basePath);
                        }
                        // TODO Delete empty directory
                        // FileUtils.deleteDirectoryIfEmpty(basePath, symLinkSrcFile.getParent());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

//    protected void onPreCommit(DatasetGraphDiff diff) {
//        diff.getRemoved().find(Node.ANY, Node.ANY, propertyNode, Node.ANY)
//            .forEachRemaining(this::processRemoval);
//
//        diff.getAdded().find(Node.ANY, Node.ANY, propertyNode, Node.ANY)
//            .forEachRemaining(this::processAddition);
//    }
//

    public Float evaluateFind(Node s, Node p, Node o) {
        Float result = propertyNode.equals(p) && (o != null && objectToPath.apply(o) != null)
                ? Float.valueOf(1.0f)
                : null;
        return result;
    }


    /**
     * The resulting stream must be closed in order to avoid 'too many open files' errors!
     *
     * @param sourceFolder
     * @param prefix
     * @param suffix
     * @return
     * @throws IOException
     */
//    public static Stream<Entry<Path, Path>> readSymbolicLinks(SymbolicLinkStrategy symlinkStrategy, Path sourceFolder, String prefix, String suffix) throws IOException {
//        return Files.list(sourceFolder)
//                .filter(symlinkStrategy::isSymbolicLink)
//                .filter(path -> {
//                    String fileName = path.getFileName().toString();
//
//                    boolean r = fileName.startsWith(prefix) && fileName.endsWith(suffix);
//                    // TODO Check that the string between prefix and suffix is either an empty string
//                    // or corresponds to a number
//                    return r;
//                })
//                .flatMap(path -> {
//                    Stream<Entry<Path, Path>> r;
//                    try {
//                        r = Stream.of(new SimpleEntry<>(path, symlinkStrategy.readSymbolicLink(path)));
//                    } catch (IOException e) {
//                        // logger.warn("Error reading symoblic link; skipping", e);
//                        r = Stream.empty();
//                    }
//                    return r;
//                });
//    }


    public org.aksw.commons.path.core.Path<String> linkTargetToKey(Txn txn, Path linkTarget) {
        // Path tgtRelFile = syncedGraph.getRootPath().relativize(linkTarget);
        Path tgtRelFile = txn.getTxnMgr().getRootPath().relativize(linkTarget);
        // Path tgtRelFile = linkTarget.relativize(txn.getTxnMgr().getRootPath());

        // deprecated: Get the path (without the filename)
        // Path tgtRelPath = tgtRelFile.getParent();
        // The file name is part of the resource key
        String[] segments = PathUtils.getPathSegments(tgtRelFile);
        org.aksw.commons.path.core.Path<String> result = PathStr.newRelativePath(segments);
        return result;
    }

    public Stream<org.aksw.commons.path.core.Path<String>> listGraphNodes(Txn txn, DatasetGraph dg, Node s, Node p, Node o) {
//    	SymlinkStrategy symlinkStrategy = extractSymlinkStrategy(dg);

        if (evaluateFind(s, p, o) == null) {
            throw new RuntimeException("Index is not suitable for lookups with predicate " + p);
        }

//        String iri = o.getURI();
//        Path relPath = UriToPathUtils.resolvePath(iri);
        String[] relPath = objectToPath.apply(o);
        //Path relPath = syncedGraph.getRelPathForIri(tgtIri);
//        String fileName = syncedGraph.getFilename();

//        String coreName = pathToFilename(relPath);
//        Path file = Paths.get(tgtFilename);
//        String prefix = MoreFiles.getNameWithoutExtension(file);
//        prefix = prefix + "." + coreName;
//        String suffix = MoreFiles.getFileExtension(file);
//        suffix = suffix.isEmpty() ? "" : "." + suffix;
//        suffix += ".link";
        // Compute prefix/suffix
        // String coreName = pathToFilename(relPath);

        Path file = Paths.get(tgtFilename);
        String tmpPrefix = MoreFiles.getNameWithoutExtension(file);
        String prefix = tmpPrefix + ".";

        String tmpSuffix = MoreFiles.getFileExtension(file);
        tmpSuffix = tmpSuffix.isEmpty() ? "" : "." + tmpSuffix;
        String suffix = tmpSuffix + ".link";



        Path symLinkSrcPath = PathUtils.resolve(basePath, relPath);
        Stream<Cell<String, Path, Path>> symLinkTgtPaths;
        try {
            try {
                symLinkTgtPaths = Files.exists(symLinkSrcPath)
                        ? SymLinkUtils.streamSymbolicLinks(symlinkStrategy, FileSyncImpl::getBaseName, prefix, suffix)
                                .apply(Files.list(symLinkSrcPath))
                        : Stream.empty();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            Stream<org.aksw.commons.path.core.Path<String>> result = symLinkTgtPaths.map(srcToTgt -> {
                Path absTgt = SymLinkUtils.resolveSymLinkAbsolute(srcToTgt.getColumnKey(), srcToTgt.getValue());
                org.aksw.commons.path.core.Path<String> r = linkTargetToKey(txn, absTgt);
                return r;
//                Path tgtRelFile = syncedGraph.getRootPath().relativize(absTgt);
//
//                // Get the path (without the filename)
//                Path tgtRelPath = tgtRelFile.getParent();
//                return tgtRelPath;
            });

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Load all graphs that are linked to and call find on the union
    }

    @Override
    public String toString() {
        return "DatasetGraphIndexerFromFileSystem [propertyNode=" + propertyNode + ", tgtFilename=" + tgtFilename + "]";
    }



//    public Iterator<Node> listGraphNodes(Node s, Node p, Node o) {
//        if (evaluateFind(s, p, o) == null) {
//            throw new RuntimeException("Index is not suitable for lookups with predicate " + p);
//        }
//
////        String iri = o.getURI();
////        Path relPath = UriToPathUtils.resolvePath(iri);
//        Path relPath = objectToPath.apply(o);
//        //Path relPath = syncedGraph.getRelPathForIri(tgtIri);
////        String fileName = syncedGraph.getFilename();
//
//        Path file = Paths.get(filename);
//        String prefix = MoreFiles.getNameWithoutExtension(file);
//        String suffix = MoreFiles.getFileExtension(file);
//        suffix = suffix.isEmpty() ? "" : "." + suffix;
//
////        Path symLinkTgtFile = relPath.resolve(fileName);
//
//
//        Path symLinkSrcPath = basePath.resolve(relPath);
//        Map<Path, Path> symLinkTgtPaths;
//        try {
//            symLinkTgtPaths = Files.exists(symLinkSrcPath)
//                    ? SymLinkUtils.readSymbolicLinks(symLinkSrcPath, prefix, suffix)
//                    : Collections.emptyMap();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        // Load all graphs that are linked to and call find on the union
//
//        IteratorConcat<Node> result = new IteratorConcat<>();
//        for (Entry<Path, Path> srcToTgt : symLinkTgtPaths.entrySet()) {
////            Path absSymLinkTgt = srcToTgt.getValue();
////            Path tgtRelPath;
////            try {
//                Path absTgt = SymLinkUtils.resolveSymLinkAbsolute(srcToTgt.getKey(), srcToTgt.getValue());
//                Path tgtRelFile = syncedGraph.getRootPath().relativize(absTgt);
//
//                // Get the path (without the filename)
//                Path tgtRelPath = tgtRelFile.getParent();
////            } catch (IOException e1) {
////                throw new RuntimeException(e1);
////            }
//            Entry<Path, Dataset> e = syncedGraph.getOrCreate(tgtRelPath);
//
//            Iterator<Node> it = e.getValue().asDatasetGraph().listGraphNodes();
//            result.add(it);
//        }
//
//        return result;
////        CatalogResolverFilesystem.allocateSymbolicLink(rawTarget, rawSourceFolder, baseName)
//    }

}
