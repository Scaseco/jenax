package org.aksw.jenax.arq.fromasfilter.assembler;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jenax.arq.fromasfilter.dataset.DatasetGraphFromAsFilter;
import org.aksw.jenax.arq.fromasfilter.model.FromAsFilterRes;
import org.aksw.jenax.arq.fromasfilter.model.GraphAlias;
import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.DatasetAssembler;
import org.apache.jena.sparql.expr.Expr;

public class DatasetAssemblerFromAsFilter
    extends DatasetAssembler
{
    @Override
    public DatasetGraph createDataset(Assembler a, Resource root) {
        FromAsFilterRes res = root.as(FromAsFilterRes.class);

         // GraphUtils.getResourceValue(root, FromAsFilterVocab.baseDataset);
        Resource baseDatasetRes = res.getBaseDataset();
        Objects.requireNonNull(baseDatasetRes, "No ja:baseDataset specified on " + root);
        Object obj = a.open(baseDatasetRes);

        Map<String, Expr> remap = null;
        Set<GraphAlias> aliases = res.getAliases();
        if (!aliases.isEmpty()) {
            remap = aliases.stream().collect(Collectors.toMap(
                GraphAlias::getGraphIri,
                GraphAlias::getExpr
            ));
        }

        DatasetGraph result;
        if (obj instanceof Dataset) {
            Dataset baseDataset = (Dataset)obj;
            result = new DatasetGraphFromAsFilter(baseDataset.asDatasetGraph(), remap);
        } else {
            Class<?> cls = obj == null ? null : obj.getClass();
            throw new AssemblerException(root, "Expected ja:baseDataset to be a Dataset but instead got " + Objects.toString(cls));
        }
        return result;
    }

}
