package org.aksw.jenax.sparql.datasource.observable;

import org.aksw.jenax.arq.util.binding.ResultSetUtils;
import org.aksw.jenax.arq.util.binding.ResultTable;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSourceWrapperBase;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngines;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSources;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;

public class RdfDataSourceObservableImpl
    extends RDFDataSourceWrapperBase
    implements RdfDataSourceObservable
{
    protected ObservableSourceImpl<Query, ResultTable> mapRx;

    public RdfDataSourceObservableImpl(RDFDataSource delegate) {
        super(delegate);
        mapRx = new ObservableSourceImpl<Query, ResultTable>(q -> execSelect(delegate, q));
    }

    @Override
    public Flowable<ResultTable> observeSelect(Query query) {
        return mapRx.observe(query);
    }

    public static ResultTable execSelect(RDFDataSource dataSource, Query query) {
        ResultTable result = RdfDataSources.exec(dataSource, query, qe -> createResultTable(qe.execSelect()));
        return result;
    }

    public static ResultTable createResultTable(ResultSet rs) {
        Table table = ResultSetUtils.resultSetToTable(rs);
        Model model = rs.getResourceModel();
        ResultTable result = new ResultTable(table, model);
        return result;
    }

    @Override
    public void refreshAll(boolean cancelRunning) {
        mapRx.refreshAll(true);
    }

    public static void main(String[] args) {
        Dataset dataset = DatasetFactory.create();
        dataset.getDefaultModel().add(RDF.type, RDF.type, RDF.Property);
        RdfDataSourceObservableImpl ds = new RdfDataSourceObservableImpl(RDFEngines.of(dataset.asDatasetGraph(), true).getLinkSource().asDataSource());

        Flowable<ResultTable> flow = ds.observeSelect(QueryFactory.create("SELECT * { ?s ?p ?o }"));
        // ds.refreshAll();
        // ds.refreshAll();

        Disposable disposable1 = flow.subscribe(t -> {
            System.out.println("1: Got table of size: " + t.getTable());
        });

        Disposable disposable2 = flow.subscribe(t -> {
            System.out.println("2: Got table of size: " + t.getTable());
        });

        System.out.println("Latest: " + flow.blockingLatest().iterator().next());

        disposable1.dispose();
        ds.refreshAll(true);

        dataset.getDefaultModel().add(RDF.type, RDFS.seeAlso, RDF.Property);
        ds.refreshAll(true);

        disposable2.dispose();

    }

}
