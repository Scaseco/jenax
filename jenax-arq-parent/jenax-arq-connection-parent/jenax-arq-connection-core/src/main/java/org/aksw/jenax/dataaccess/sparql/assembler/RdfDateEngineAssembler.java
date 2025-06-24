package org.aksw.jenax.dataaccess.sparql.assembler;

import static org.apache.jena.sparql.util.graph.GraphUtils.getResourceValue;

import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.JA;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.assembler.DatasetAssembler;
import org.apache.jena.sparql.core.assembler.NamedDatasetAssembler;

/**
 * Jena's {@link DatasetAssembler} adapted for {@link RDFEngine}.
 */
public abstract class RdfDateEngineAssembler extends AssemblerBase {

    public static final String NS = JA.getURI() ;
    public static final Resource tRdfDatasEngine            = ResourceFactory.createResource(NS+"RdfDataEngine") ;

    /** This is the superclass of all datasets assemblers */
    public static Resource getGeneralType() {
        return tRdfDatasEngine ;
    }

    @Override
    public RDFEngine open(Assembler a, Resource root, Mode mode) {
        RDFEngine result = createNamedEngine(a, root) ;
        return result;
        // return DatasetFactory.wrap(dsg);
    }

    /**
     * Indirection to allow subclasses to have a pool of created datasets
     * (e.g. {@link NamedDatasetAssembler}).
     * <p>
     * Not used by TDB with a location because databases required
     * to be shared system-wide by location. This includes in-memory
     * named locations.
     */
    protected RDFEngine createNamedEngine(Assembler a, Resource root) {
        return createEngine(a, root);
    }

    /**
     * Create a fresh dataset from the description.
     */
    protected abstract RDFEngine createEngine(Assembler a, Resource root);

    /**
     * Helper for datasets that layer on top of other datasets.
     * The property is usually {@code ja:dataset}.
     * Assemble a DatasetGraph from description referred to by resource-property.
     */
    protected RDFEngine createBaseDataset(Resource dbAssem, Property pDataset) {
        Resource dataset = getResourceValue(dbAssem, pDataset) ;
        if ( dataset == null )
            throw new AssemblerException(dbAssem, "Required base dataset missing: "+dbAssem) ;
        RDFEngine result = (RDFEngine)Assembler.general.open(dataset);
        // return base.asDatasetGraph();
        return result;
    }
}
