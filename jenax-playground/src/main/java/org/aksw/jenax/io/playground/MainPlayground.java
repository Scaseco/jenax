package org.aksw.jenax.io.playground;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.riot.lang.LabelToNode;
import org.apache.jena.riot.system.AsyncParser;
import org.apache.jena.riot.tokens.Token;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerText;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.system.AutoTxn;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDB2Factory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;


class Table {
    protected String hashFileBaseName;
    protected List<FileChannel> hashFileChannels = new ArrayList<>();

    protected Path mapFile;
    protected FileChannel mapFileChannel;
    protected long mapChannelOffset;

    protected Cache<Long, Node> offsetToNode = Caffeine.newBuilder().maximumSize(10000).build();
    protected Cache<Node, Long> nodeToOffset = Caffeine.newBuilder().maximumSize(10000).build();


    public Table() throws IOException {
        // this.hashFileBaseName = "/tmp/hashfile.dat";
        this.mapFile = Path.of("/tmp/mapfile.dat");
        this.mapFileChannel = FileChannel.open(mapFile, StandardOpenOption.SPARSE, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        mapFileChannel.map(MapMode.READ_WRITE, 0, 1).put((byte)0);
        this.mapChannelOffset = this.mapFileChannel.size();
    }

//    protected void ensureOpen() throws IOException {
//        // Files.open(hashFilePath, StandardOpenOption.SPARSE);
//        FileChannel fc = FileChannel.open(hashFile, StandardOpenOption.SPARSE);
//        MappedByteBuffer map = fc.map(MapMode.READ_WRITE, 0, 8096);
//
//        // 4byte hash: [5 * 8byte offsets]
//        // if slot is full - map to a new file - with same offset?
//        // or map to some buffer?
//        // map hash to 16 slots? - but then wed need 2gb * 8byte * 5 -> 80gb.
//    }

    protected FileChannel getHashFileChannel(int index) throws IOException {
        FileChannel result;
        if (index >= hashFileChannels.size()) {
            Path newHashFile = Path.of("/tmp/mapfile" + index + ".dat");
            result = FileChannel.open(newHashFile, StandardOpenOption.SPARSE, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            hashFileChannels.add(result);
        } else {
            result = hashFileChannels.get(index);
        }
        return result;
    }

    public long put(Node node) throws IOException {
        if (!node.isURI()) {
            return -1;
        }

//        Long tmp = nodeToOffset.getIfPresent(node);
//        if (tmp != null) {
//            return tmp;
//        }

        long result;
        long hash = Math.abs(node.hashCode());

        outer: for (int i = 0; ; ++i) {
            FileChannel fc = getHashFileChannel(i);
            MappedByteBuffer buf = fc.map(MapMode.READ_WRITE, hash * 8l, 40);
            int bufOffset = 0;
            for (;;) {
                long offset = buf.getLong();
                bufOffset += 8;
                if (offset == 0) { // Add the new node
                    result = getOffsetForNode(node, buf, bufOffset);
                    // nodeToOffset.put(node, result);
                    break outer;
                } else {
                    Node entryNode = readNodeAtOffset(offset);
//                    Node entryNode = offsetToNode.get(offset, o -> {
//                        try {
//                            return readNodeAtOffset(o);
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//                    });

                    if (Objects.equals(node, entryNode)) {
                        result = offset;
                        break outer;
                    }
                }

                if (bufOffset >= 40) {
                    break;
                }
            }
        }

        return result;
    }

    private long getOffsetForNode(Node node, MappedByteBuffer buf, int bufOffset) throws IOException {
        long result;
        buf.position(bufOffset - 8);
        // Allocate a new node entry.
        byte[] nodeBytes = node.getURI().getBytes(StandardCharsets.UTF_8); // NodeFmtLib.strNT(node).getBytes(StandardCharsets.UTF_8);
        int dataLen = 4 + nodeBytes.length;
        MappedByteBuffer mapBuf = mapFileChannel.map(MapMode.READ_WRITE, mapChannelOffset, dataLen);
        mapBuf.putInt(nodeBytes.length);
        mapBuf.put(nodeBytes);
        result = mapChannelOffset;
        buf.asLongBuffer().put(result);
        offsetToNode.put(result, node);
        mapChannelOffset += dataLen;
        return result;
    }

    private Node readNodeAtOffset(long offset) throws IOException {
        // System.out.println("read offset: " + offset);
        // Read the node and check whether it matches the given one
        MappedByteBuffer mapBuf1 = mapFileChannel.map(MapMode.READ_ONLY, offset, 4);
        int len = mapBuf1.asIntBuffer().get();
        byte[] data = new byte[len];
        MappedByteBuffer mapBuf2 = mapFileChannel.map(MapMode.READ_ONLY, offset + 4,  len);
        mapBuf2.get(data);
        String str = new String(data, StandardCharsets.UTF_8);
        Node entryNode = NodeFactory.createURI(str); // parseNode(str);
        return entryNode;
    }

    /** Expect the string to parse as a single node. */
    public static Node parseNode(String str) {
        Node result = null;
        // NodeFmtLib.strNodes encodes labels - so we need to decode them
        LabelToNode decoder = LabelToNode.createUseLabelEncoded();

        Tokenizer tokenizer = TokenizerText.create().fromString(str).build();
        try {
            if (tokenizer.hasNext()) {
                Token token = tokenizer.next() ;
                Node node = token.asNode() ;
    //            if ( node == null )
    //                throw new RiotException("Bad RDF Term: " + str) ;

                if (node != null) {
                    if (node.isBlank()) {
                        String label = node.getBlankNodeLabel();
                        node = decoder.get(null, label);
                    }
                }

                result = node;
            }

            if (tokenizer.hasNext()) {
                throw new IllegalArgumentException("String parsed into more than 1 node.");
            }
        } finally {
            tokenizer.close();
        }

        return result;
    }

    public void close() throws IOException {
        for (FileChannel fc : hashFileChannels) {
            fc.close();
        }

        mapFileChannel.close();
    }
}

public class MainPlayground {
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();

        Table table = new Table();

        // Dataset ds = TDB2Factory.connectDataset(Location.create(Path.of("/tmp/testtdb")));

        // try (AutoTxn txn = Txn.autoTxn(ds, ReadWrite.WRITE)) {
            String fileName = "/home/raven/.local/share/Trash/files/sansa-copy/sansa-resource-testdata/src/main/resources/hobbit-sensor-stream-150k-events-data.trig.bz2";
            try (Stream<Quad> stream = AsyncParser.of(fileName).streamQuads()) {
                // stream.forEach(q -> System.out.println(q));
                Iterator<Quad> it = stream.iterator();
                while (it.hasNext()) {
                    Quad q = it.next();

                    long g = table.put(q.getGraph());
                    long s = table.put(q.getSubject());
                    long p = table.put(q.getPredicate());
                    long o = table.put(q.getObject());

                    // ds.asDatasetGraph().add(q);

                    // System.out.println(g + " " + s + " " + p + " " + o);
                }
            }
            //txn.commit();
        // }
        table.close();

        long end = System.currentTimeMillis();
        System.out.println("Elapsed: " + (end - start) * 0.001f);
    }
}
