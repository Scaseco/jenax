package org.aksw.jenax.dataaccess.sparql.creator;

import java.io.IOException;
import java.nio.file.Path;

import org.aksw.commons.util.obj.HasSelf;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdflink.RDFLinkDataset;

/** Essentially a batching version of the {@link RDFLinkDataset} API. */
public interface RdfDatabaseBuilder<X extends RdfDatabaseBuilder<X>>
    extends HasSelf<X>
{
    /** The the folder of the database location. */
    X setOutputFolder(Path outputFolder);

    /** Set the database name. May not be supported by the underlying database management system. */
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
    RdfDatabase build() throws IOException, InterruptedException;
}
