package org.aksw.jenax.sparql.rx.op;

import java.util.Iterator;

import org.aksw.commons.rx.function.RxFunction;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParserImpl;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.syntax.Template;

import com.google.common.base.Preconditions;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;


public class FlowOfBindingsOps {


    /**
     * Bride from Flowable&lt;Binding&gt; to QueryIterator.
     * Delegates the requestCancel and closeIterator methods.
     *
     * Obtain a blocking iterable from the flow and wrap it as a QueryIterator.
     * Closing the latter cascades to the disaposable obtained from the flowable.
     */
    public static QueryIterator toQueryIterator(Flowable<Binding> bindingFlow) {
        Iterator<Binding> tmp = bindingFlow.blockingIterable().iterator();
        QueryIterator result = new QueryIterPlainWrapper(tmp) {
            @Override
            protected void requestCancel() {
                ((Disposable)tmp).dispose();
                super.requestCancel();
            }

            @Override
            protected void closeIterator() {
                ((Disposable)tmp).dispose();
                super.closeIterator();
            }
        };

        return result;
    }

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
