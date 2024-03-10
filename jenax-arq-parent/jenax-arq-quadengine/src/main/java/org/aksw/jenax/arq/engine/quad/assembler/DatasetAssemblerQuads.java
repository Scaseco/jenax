package org.aksw.jenax.arq.engine.quad.assembler;

import java.util.Objects;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.DatasetAssembler;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.main.QueryEngineMainQuad;
import org.apache.jena.sparql.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper Dataset that configures the quad engine in the underlying dataset's context
 *
 *
 */
public class DatasetAssemblerQuads
    extends DatasetAssembler
{
    private static final Logger logger = LoggerFactory.getLogger(DatasetAssemblerQuads.class);

    @Override
    public DatasetGraph createDataset(Assembler a, Resource root) {
        RdfDatasetAssemblerQuads res = root.as(RdfDatasetAssemblerQuads.class);

        Resource baseDatasetRes = res.getDataset();
        Objects.requireNonNull(baseDatasetRes, "No ja:bataset specified on " + root);
        Object obj = a.open(baseDatasetRes);

        DatasetGraph result;
        if (obj instanceof Dataset) {
            logger.info("Configuring quad engine on dataset with id " + baseDatasetRes.asNode());

            Dataset dataset = (Dataset)obj;
            Context cxt = dataset.getContext();

            QueryEngineRegistry reg = new QueryEngineRegistry();
            reg.add(QueryEngineMainQuad.getFactory());
            QueryEngineRegistry.set(cxt, reg);

            result = dataset.asDatasetGraph();
        } else {
            Class<?> cls = obj == null ? null : obj.getClass();
            throw new AssemblerException(root, "Expected ja:baseDataset to be a Dataset but instead got " + Objects.toString(cls));
        }
        return result;
    }
}
