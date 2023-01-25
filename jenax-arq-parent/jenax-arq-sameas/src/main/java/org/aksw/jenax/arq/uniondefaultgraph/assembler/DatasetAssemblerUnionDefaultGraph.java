package org.aksw.jenax.arq.uniondefaultgraph.assembler;

import java.util.Objects;

import org.aksw.jenax.arq.sameas.model.SameAsConfig;
import org.aksw.jenax.arq.util.dataset.DatasetGraphWrapperUnionDefaultGraph;
import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.DatasetAssembler;

public class DatasetAssemblerUnionDefaultGraph
    extends DatasetAssembler
{
    @Override
    public DatasetGraph createDataset(Assembler a, Resource root) {
        SameAsConfig res = root.as(SameAsConfig.class);

        Resource baseDatasetRes = res.getBaseDataset();
        Objects.requireNonNull(baseDatasetRes, "No ja:baseDataset specified on " + root);
        Object obj = a.open(baseDatasetRes);
        DatasetGraph result;
        if (obj instanceof Dataset) {
            Dataset baseDataset = (Dataset)obj;
            result = DatasetGraphWrapperUnionDefaultGraph.wrapIfNeeded(baseDataset.asDatasetGraph());
        } else {
            Class<?> cls = obj == null ? null : obj.getClass();
            throw new AssemblerException(root, "Expected ja:baseDataset to be a Dataset but instead got " + Objects.toString(cls));
        }
        return result;
    }

}
