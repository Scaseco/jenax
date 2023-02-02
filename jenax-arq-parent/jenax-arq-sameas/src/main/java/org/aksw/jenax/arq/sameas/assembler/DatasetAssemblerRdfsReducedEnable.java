package org.aksw.jenax.arq.sameas.assembler;

import static org.apache.jena.sparql.util.graph.GraphUtils.getAsStringValue;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdfs.RDFSFactory;
import org.apache.jena.rdfs.assembler.VocabRDFS;
import org.apache.jena.rdfs.setup.ConfigRDFS;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.DatasetAssembler;

/**
 * An assembler that enables the use of the service
 * plugins SERVICE <sameAs:> {}, SERVICE <rdfs:> {} and SERVICE <sameAs+rdfs> {}.
 * This assembler only registers the specified RDFS setup in the dataset context such that the
 * plugins can pick it up.
 *
 * In other words, this assembler only sets a context attribute in the base dataset and returns it.
 * [] ja:context [ ja:cxtName "rdfsSetupNode" ;  ja:cxtValue [ a :RDFSSetup ; ja:rdfsSchema "rdfs.ttl" ] ] ;
 */
public class DatasetAssemblerRdfsReducedEnable extends DatasetAssembler {

    public static final Resource TYPE = ResourceFactory.createResource(SameAsTerms.NS + "DatasetRDFSEnabled");

    public static Resource getType() {
        return TYPE;
    }

    @Override
    public DatasetGraph createDataset(Assembler a, Resource root) {
        DatasetGraph base = super.createBaseDataset(root, VocabRDFS.pDataset);
        if ( base == null )
            throw new AssemblerException(root, "Required base dataset missing: "+VocabRDFS.pDataset) ;

        String schemaFile = getAsStringValue(root, VocabRDFS.pRdfsSchemaFile);
        if ( schemaFile == null )
            throw new AssemblerException(root, "Required property missing: "+VocabRDFS.pRdfsSchemaFile) ;

        Graph schema = RDFDataMgr.loadGraph(schemaFile);
        ConfigRDFS<Node> setup = RDFSFactory.setupRDFS(schema);
        base.getContext().set(DatasetAssemblerRdfsReduced.symSetupRdfsNode, setup);

        return base;
    }
}