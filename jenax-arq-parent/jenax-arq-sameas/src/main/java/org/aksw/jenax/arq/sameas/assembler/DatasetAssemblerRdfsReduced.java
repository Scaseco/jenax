package org.aksw.jenax.arq.sameas.assembler;

import static org.apache.jena.sparql.util.graph.GraphUtils.getAsStringValue;

import org.aksw.jenax.arq.util.dataset.DatasetGraphRDFSReduced;
import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdfs.RDFSFactory;
import org.apache.jena.rdfs.SetupRDFS;
import org.apache.jena.rdfs.assembler.DatasetRDFSAssembler;
import org.apache.jena.rdfs.assembler.VocabRDFS;
import org.apache.jena.rdfs.setup.ConfigRDFS;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.DatasetAssembler;
import org.apache.jena.sparql.util.Symbol;

/**
 * Same as {@link DatasetRDFSAssembler} except that the type is jxp:DatasetRDFS
 */
public class DatasetAssemblerRdfsReduced extends DatasetAssembler {

    // Symbol for putting the RDFS T-BOX into a dataset's context (for Nodes rather than NodeIDs)
    public static final Symbol symSetupRdfsNode = SystemARQ.allocSymbol("setupRdfsNode");

    public static final Resource TYPE = ResourceFactory.createResource(SameAsTerms.NS + "DatasetRDFS");

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
        base.getContext().set(symSetupRdfsNode, setup);

        DatasetGraph dsg = DatasetGraphRDFSReduced.wrap(base, setup);
        return dsg;
    }
}