package org.aksw.jenax.dataaccess.sparql.linksource.track;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.JA;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdfs.assembler.VocabRDFS;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.DatasetAssembler;

/**
 * Assembler implementation that adds {@linkplain DatasetGraphWithExecTracker} wrapping.
 */
public class DatasetAssemblerExecTracker extends DatasetAssembler {
    public static final Resource TYPE = ResourceFactory.createResource(JA.uri + "TrackedDataset");

    public static Resource getType() {
        return TYPE;
    }

    @Override
    public DatasetGraph createDataset(Assembler a, Resource root) {

        DatasetGraph base = super.createBaseDataset(root, VocabRDFS.pDataset);
        if ( base == null )
            throw new AssemblerException(root, "Required base dataset missing: "+VocabRDFS.pDataset) ;

        DatasetGraph result = DatasetGraphWithExecTracker.wrap(base);
        return result;
    }
}
