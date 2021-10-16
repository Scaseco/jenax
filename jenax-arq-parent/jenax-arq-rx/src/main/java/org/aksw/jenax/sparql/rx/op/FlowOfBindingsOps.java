package org.aksw.jenax.sparql.rx.op;

import org.aksw.commons.rx.function.RxFunction;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParserImpl;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.Template;

import com.google.common.base.Preconditions;


public class FlowOfBindingsOps {

    /**
     * Returns a serializable RxFunction that maps bindings in
     * tarql like fashion. This means each binding is used as input to the provided query.
     *
     *
     * @param query
     * @return
     */
    public static RxFunction<Binding, Dataset> tarqlDatasets(Query query) {
        Preconditions.checkArgument(query.isConstructType(), "Construct query expected");

        String queryStr = query.toString();
        return upstream -> {
            Query q = SparqlQueryParserImpl.createAsGiven().apply(queryStr);
            Template template = q.getConstructTemplate();
            Op op = Algebra.compile(q);

            return upstream
                    .compose(QueryFlowOps.createMapperBindings(op))
                    .flatMap(QueryFlowOps.createMapperQuads(template)::apply)
                    .reduceWith(DatasetGraphFactory::create, (dsg, quad) -> { dsg.add(quad); return dsg; })
                    .map(DatasetFactory::wrap)
                    .toFlowable();
        };
    }

}
