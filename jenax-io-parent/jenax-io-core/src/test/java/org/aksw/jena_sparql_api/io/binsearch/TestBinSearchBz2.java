package org.aksw.jena_sparql_api.io.binsearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.aksw.commons.cache.async.AsyncClaimingCacheImpl;
import org.aksw.commons.cache.async.AsyncClaimingCacheImpl.Builder;
import org.aksw.commons.io.binseach.BinarySearcher;
import org.aksw.commons.io.block.api.PageManager;
import org.aksw.commons.io.block.impl.Page;
import org.aksw.commons.io.block.impl.PageManagerForFileChannel;
import org.aksw.commons.io.block.impl.PageManagerOverDataStreamSource;
import org.aksw.commons.io.hadoop.binseach.bz2.BlockSources;
import org.aksw.commons.io.input.ReadableChannel;
import org.aksw.commons.io.input.ReadableChannelSource;
import org.aksw.commons.io.input.ReadableChannelSources;
import org.aksw.commons.io.input.ReadableChannels;
import org.aksw.commons.io.seekable.api.Seekable;
import org.aksw.commons.io.seekable.api.SeekableSource;
import org.aksw.commons.io.seekable.api.SeekableSources;
import org.aksw.commons.io.seekable.impl.SeekableSourceFromPageManager;
import org.aksw.commons.io.seekable.impl.SeekableSourceOverDataStreamSource;
import org.aksw.jenax.sparql.query.rx.RDFDataMgrRx;
import org.aksw.jenax.sparql.rx.op.GraphOpsRx;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterators;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;


public class TestBinSearchBz2 {

    private static Logger logger = LoggerFactory.getLogger(TestBinSearchBz2.class);

    @Test
    public void testBinarySearchBz2Lookups() throws IOException {

        Path path = Paths.get("src/test/resources/2015-11-02-Amenity.node.5mb-uncompressed.sorted.nt.bz2");
        Map<Node, Graph> expected = loadData(path);

        // testSeekableOverDataStreamSource(path);

        // runBinSearchOnBzip2ViaFileChannel(path, expected);
        runBinSearchOnBzip2ViaPath(path, expected);
    }

    public static Map<Node, Graph> loadData(Path path) {
        // Read file and map each key to the number of lines
        Stopwatch sw = Stopwatch.createStarted();
        Map<Node, Graph> result = RDFDataMgrRx.createFlowableTriples(
                    () -> new BZip2CompressorInputStream(Files.newInputStream(path, StandardOpenOption.READ), true),
                    Lang.NTRIPLES, null)
                .compose(GraphOpsRx.graphsFromConsecutiveSubjectsRaw())
                .toMap(Entry::getKey, Entry::getValue)
                .blockingGet()
                ;

        // Note that the logged time is for cold state - repeated loads should
        // exhibit significant speedups
        logger.debug("Needed " + (sw.elapsed(TimeUnit.MILLISECONDS) * 0.001) + " seconds to load " + path);
        return result;
    }

//    public static void main(String[] args) throws IOException {
//        runTest();
//    }

    @Test
    public void testFullRead() throws IOException {
        Path path = Paths.get("src/test/resources/2015-11-02-Amenity.node.5mb-uncompressed.sorted.nt.bz2");
//        Path path = Paths.get("/home/raven/tmp/sorttest/sorted.nt.bz2");

        try(FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
            BufferedReader expected = new BufferedReader(new InputStreamReader(new BZip2CompressorInputStream(Files.newInputStream(path, StandardOpenOption.READ), true)))) {
            BinarySearcher bs = BlockSources.createBinarySearcherBz2(fileChannel, PageManagerForFileChannel.DEFAULT_PAGE_SIZE, false);

            BufferedReader actual = new BufferedReader(new InputStreamReader(bs.search((String)null)));

            String lineActual;
            String lineExpected;
            for(int i = 1; ; ++i) {
                lineActual = actual.readLine();
                lineExpected = expected.readLine();

//                System.out.println("Line: " + i);
                if(!Objects.equals(lineExpected, lineActual)) {
                    logger.warn("Mismatch in line " + i);
                    Assert.assertEquals(lineExpected, lineActual);
                }

                if(lineActual == null && lineExpected == null) {
                    break;
                }
            }

        }

    }

    /**
     * For future reference and quantifying improvements:
     * The first working version of this test took [11.6, 11.3, 11.5] seconds ~ 2020-05-08 Claus Stadler
     *
     *
     *
     *
     */
    public static void runBinSearchOnBzip2ViaFileChannel(Path path, Map<Node, Graph> expectedResults) throws IOException {
        Stopwatch sw = Stopwatch.createStarted();

        int i = 0;
        try(FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            BinarySearcher bs = BlockSources.createBinarySearcherBz2(fileChannel, PageManagerForFileChannel.DEFAULT_PAGE_SIZE, false);

            // This key overlaps on the block boundary (byte 2700000)
//            try(InputStream in = bs.search("<http://linkedgeodata.org/geometry/node1012767568>")) {
//                MainPlaygroundScanFile.printLines(in, 10);
//            }
//

            // Generic tests

            for(Entry<Node, Graph> e : expectedResults.entrySet()) {
                Node s = e.getKey();
                ++i;
//                System.out.println("Test #" + (++i) + ": " + s);
                Graph expected = e.getValue();

//                if(s.getURI().equals("http://linkedgeodata.org/geometry/node1012767568")) {
//                    System.err.println("DEBUG POINT");
//                }

                //String str = s.isURI() ? "<" + s.getURI() + ">" : s.getBlankNodeLabel()
                String str = NodeFmtLib.str(s);
                try(InputStream in = bs.search(str)) {
                    Graph actual = GraphFactory.createDefaultGraph();
                    RDFDataMgr.read(actual, in, Lang.NTRIPLES);


                    // Assert.assertEquals(expected, actual);
                    boolean isOk = expected.isIsomorphicWith(actual);
                    if(!isOk) {
                        System.err.println("Expected:");
                        RDFDataMgr.write(System.err, expected, RDFFormat.TURTLE_PRETTY);
                        System.err.println("Actual:");
                        RDFDataMgr.write(System.out, actual, RDFFormat.TURTLE_PRETTY);
                    }

                    // System.out.println("Iteration #" + i + ": ok? " + isOk);
                    Assert.assertTrue(isOk);
                }

            }

        }

        logger.debug("Needed " + (sw.elapsed(TimeUnit.MILLISECONDS) * 0.001) + " seconds for " + i + " lookups on " + path);

    }

    public static void testSeekableOverDataStreamSource(Path path) throws IOException {

        // Test to compare seeking on a DataStream directly and using the Seekable abstraction
        ReadableChannelSource<byte[]> source = ReadableChannelSources.of(path, true);
        SeekableSource ss = SeekableSources.of(source, 4096, 128); // new SeekableSourceOverDataStreamSource(source, 100);

        int size = Ints.saturatedCast(source.size());
        int maxDelta = size / 10;

        Random random = new Random(0);
        for (int i = 0; i < 30; ++i) {
            long baseOffset = random.nextInt(size);
            try (Seekable seekable = ss.get(baseOffset)) {
                long offset = baseOffset;
                int requestDelta = random.nextInt(maxDelta) - (maxDelta >> 1);
                int actualDelta;
                if (requestDelta >= 0) {
                    actualDelta = seekable.checkNext(requestDelta, true);
                    offset += actualDelta;
                } else {
                    actualDelta = seekable.checkPrev(-requestDelta, true);
                    offset -= actualDelta;
                }

                try (ReadableChannel<byte[]> raw = source.newReadableChannel(Range.atLeast(offset))) {

                    // Check reading of a single byte
                    Byte expectedByte = Iterators.getNext(ReadableChannels.newBoxedIterator(raw, 1), null);
                    Byte actualByte = seekable.get();
                    Assert.assertEquals(expectedByte, actualByte);

                    seekable.nextPos(1);

                    // Check reading of all remaining data
                    byte[] expecteds = IOUtils.toByteArray(Channels.newInputStream(ReadableChannels.newChannel(raw)));
                    byte[] actuals = IOUtils.toByteArray(Channels.newInputStream(seekable));

                    Assert.assertArrayEquals(expecteds, actuals);
                }
            }
        }
    }

    public static void runBinSearchOnBzip2ViaPath(Path path, Map<Node, Graph> expectedResults) throws IOException {

        ReadableChannelSource<byte[]> source = ReadableChannelSources.of(path);
        source = ReadableChannelSources.cacheInMemory(source, 1024 * 1024, 128, Long.MAX_VALUE);
        SeekableSource seekableSource = SeekableSources.of(source, 1024 * 1024, 128);


        Stopwatch sw = Stopwatch.createStarted();

        int i = 0;

        BinarySearcher bs = BlockSources.createBinarySearcherBz2(seekableSource);

        // This key overlaps on the block boundary (byte 2700000)
//            try(InputStream in = bs.search("<http://linkedgeodata.org/geometry/node1012767568>")) {
//                MainPlaygroundScanFile.printLines(in, 10);
//            }
//

        // Generic tests

        for(Entry<Node, Graph> e : expectedResults.entrySet()) {
            Node s = e.getKey();
            ++i;
//                System.out.println("Test #" + (++i) + ": " + s);
            Graph expected = e.getValue();

//                if(s.getURI().equals("http://linkedgeodata.org/geometry/node1012767568")) {
//                    System.err.println("DEBUG POINT");
//                }

            //String str = s.isURI() ? "<" + s.getURI() + ">" : s.getBlankNodeLabel()
            String str = NodeFmtLib.str(s);
            try(InputStream in = bs.search(str)) {
                Graph actual = GraphFactory.createDefaultGraph();
                RDFDataMgr.read(actual, in, Lang.NTRIPLES);


                // Assert.assertEquals(expected, actual);
                boolean isOk = expected.isIsomorphicWith(actual);
                if(!isOk) {
                    System.err.println("Expected:");
                    RDFDataMgr.write(System.err, expected, RDFFormat.TURTLE_PRETTY);
                    System.err.println("Actual:");
                    RDFDataMgr.write(System.out, actual, RDFFormat.TURTLE_PRETTY);
                }

                // System.out.println("Iteration #" + i + ": ok? " + isOk);
                Assert.assertTrue(isOk);
            }

        }

        logger.debug("Needed " + (sw.elapsed(TimeUnit.MILLISECONDS) * 0.001) + " seconds for " + i + " lookups on " + path);

    }

//    @Test
    public void testLocalBinSearch() throws IOException, Exception {
        try(BinarySearcher bs = BlockSources.createBinarySearcherBz2(Paths.get("/home/raven/tmp/sorttest/dnb-all_lds_20200213.sorted.nt.bz2"))) {
            try (InputStream in = bs.search("<https://d-nb.info/1017454930>")) {
                System.out.println("Output: " + IOUtils.toString(in, StandardCharsets.UTF_8));
            }
        }
    }

}

//public static void doAssert(BinarySearcher searcher, String key, int expectedLines) throws IOException {
//    try(InputStream in = searcher.search(key)) {
//        List<String> lines = new BufferedReader(new InputStreamReader(in))
//                .lines().collect(Collectors.toList());
//        //MainPlaygroundScanFile.printLines(in, 5);
//        int actual = lines.size();
//        Assert.assertEquals(expectedLines, actual);
//    }
//}
//
            // A record in the middle of a block
//             String str = "<http://linkedgeodata.org/geometry/node1583470199>";

            // This one is the first record in a block:
//            String str = "<http://linkedgeodata.org/geometry/node1583253778>";

            // This one is overlapping before node1583253778
//            String str = "<http://linkedgeodata.org/geometry/node1583253655>";

            // This key is on the first page and the key itself overlaps with the page boundary
             //String str = "<http://linkedgeodata.org/geometry/node1003603551>";

            // First key on first page
//            String str = "<http://linkedgeodata.org/geometry/node1000005269>";


            // Second key on first page
//            String str = "<http://linkedgeodata.org/geometry/node1000006271>";

            // Mistyped key (missing '<')
            // doAssert(bs, "http://linkedgeodata.org/geometry/node1000005269", 0);

            // Empty string should match everything - needs special handling?
            // String str = "";
//            try(InputStream in = bs.search(str)) {
//                MainPlaygroundScanFile.printLines(in, 5);
//            }
