package org.aksw.jenax.arq.sameas.assembler;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.aksw.jenax.arq.sameas.dataset.DatasetGraphSameAs;
import org.aksw.jenax.arq.sameas.model.SameAsConfig;
import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.DatasetAssembler;
import org.apache.jena.vocabulary.OWL;

public class DatasetAssemblerSameAs
    extends DatasetAssembler
{
    @Override
    public DatasetGraph createDataset(Assembler a, Resource root) {
        SameAsConfig res = root.as(SameAsConfig.class);

         // GraphUtils.getResourceValue(root, FromAsFilterVocab.baseDataset);
        Resource baseDatasetRes = res.getBaseDataset();
        Objects.requireNonNull(baseDatasetRes, "No ja:baseDataset specified on " + root);
        Object obj = a.open(baseDatasetRes);

        int cacheMaxSize = Optional.ofNullable(res.getCacheMaxSize()).orElse(DatasetGraphSameAs.DFT_MAX_CACHE_SIZE);
        Set<Node> predicates = new LinkedHashSet<>(res.getPredicates());
        if (predicates.isEmpty()) {
            predicates.add(OWL.sameAs.asNode());
        }

        DatasetGraph result;
        if (obj instanceof Dataset) {
            Dataset baseDataset = (Dataset)obj;
            result = DatasetGraphSameAs.wrap(baseDataset.asDatasetGraph(), predicates, cacheMaxSize);
        } else {
            Class<?> cls = obj == null ? null : obj.getClass();
            throw new AssemblerException(root, "Expected ja:baseDataset to be a Dataset but instead got " + Objects.toString(cls));
        }
        return result;
    }

}
