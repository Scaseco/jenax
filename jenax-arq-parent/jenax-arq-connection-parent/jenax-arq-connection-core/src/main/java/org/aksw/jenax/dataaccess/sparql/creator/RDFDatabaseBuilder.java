package org.aksw.jenax.dataaccess.sparql.creator;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdflink.RDFLinkDataset;

/** Essentially a batching version of the {@link RDFLinkDataset} API. */
public interface RDFDatabaseBuilder<X extends RDFDatabaseBuilder<X>>
    extends HasProperties<X>
    // extends HasSelf<X>
{
    /** The the folder of the database location. */
    X setOutputFolder(Path outputFolder);

    /**
     * Set the database name.
     * Interpretation of this property depends on the implementation.
     * Typically, either a sub folder in the output folder is created,
     * or the name is used as a file prefix.
     */
    X setName(String name);

    /** Prepare an RDF file for loading. If the data is triple-based it will be added to the
     *  currently set graph. Builder implementations may eagerly validate the added path. */
    X addPath(String source, Node graph) throws IOException;

    default X addPath(String source) throws IOException {
        return addPath(source, (Node)null);
    }

    default X addPath(String source, String graph) throws IOException {
        Node graphNode = graph == null ? null : NodeFactory.createURI(graph);
        return addPath(source, graphNode);
    }

//    default X addPath(String source, String graph, String sourceFormat) throws IOException {
//        Node graphNode = graph == null ? null : NodeFactory.createURI(graph);
//        return addPath(source, graphNode);
//    }

//  default X addPath(String source, String graph, String sourceFormat, List<String> sourceEncodings) throws IOException {
//  Node graphNode = graph == null ? null : NodeFactory.createURI(graph);
//  return addPath(source, graphNode);
//}

    /**
     * Determine the types of arguments:
     * If all files are directly nq or ttl then use them as file arguments.
     * Otherwise, build a stream from the argument:
     * - A mix of nq and ttl is not supported.
     * - Use cat or a codec to decode files
     * - Use a flag whether to use process substitution or command grouping.
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    // TODO Should probably return a future to allow for concurrent cancel
    RDFDatabase build() throws IOException, InterruptedException;
}
