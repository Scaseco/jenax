package org.aksw.jenax.arq.sameas.assembler;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.aksw.jenax.arq.dataset.cache.CachePatterns;
import org.aksw.jenax.arq.dataset.cache.DatasetGraphCache;
import org.aksw.jenax.arq.sameas.dataset.DatasetGraphSameAsOld;
import org.aksw.jenax.arq.sameas.model.SameAsConfig;
import org.aksw.jenax.arq.util.dataset.DatasetGraphSameAs;
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

        int cacheMaxSize = Optional.ofNullable(res.getCacheMaxSize()).orElse(0);
        boolean allowDuplicates = Optional.ofNullable(res.getAllowDuplicates()).orElse(false);

        Set<Node> predicates = new LinkedHashSet<>(res.getPredicates());
        if (predicates.isEmpty()) {
            predicates.add(OWL.sameAs.asNode());
        }

        DatasetGraph result;
        if (obj instanceof Dataset) {
            Dataset baseDataset = (Dataset)obj;
            DatasetGraph base = baseDataset.asDatasetGraph();

            // A negative value for caching loads all patterns into the cache
            // The idea is that if we know that e.g. all outgoing/incoming sameAs links are cached
            // then whenever there is a cache miss we know that there is no data and can skip the request
            // to the backing graph
            if (cacheMaxSize > 0) {
                DatasetGraph cache = DatasetGraphCache.cache(base, CachePatterns.forNeigborsByPredicates(predicates), cacheMaxSize);
                result = DatasetGraphSameAs.wrap(cache, predicates, allowDuplicates);
            } else if (cacheMaxSize < 0) {
                // base = DatasetGraphCache.table(base, CachePatterns.forNeigborsByPredicates(predicates));
                result = DatasetGraphSameAs.wrapWithTable(base, predicates, allowDuplicates);
            } else {
                result = DatasetGraphSameAs.wrap(base, predicates, allowDuplicates);
            }

           // result = DatasetGraphSameAs.wrap(base, predicates, allowDuplicates);
           // result = DatasetGraphSameAsOld.wrap(base, predicates, allowDuplicates);
        } else {
            Class<?> cls = obj == null ? null : obj.getClass();
            throw new AssemblerException(root, "Expected ja:baseDataset to be a Dataset but instead got " + Objects.toString(cls));
        }
        return result;
    }
}
