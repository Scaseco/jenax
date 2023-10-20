package org.aksw.jena_sparql_api.lookup;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.commons.rx.lookup.MapService;
import org.aksw.jena_sparql_api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.jena_sparql_api.delay.extra.DelayerDefault;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryDataset;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.impl.ConceptUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sys.JenaSystem;
import org.junit.Test;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TestReactiveSparql {

    static { JenaSystem.init(); }


    //@Test
    public void testSelectLookupSimple() {
        // RDFConnection conn = RDFConnectionFactory.connect(RDFDataMgr.loadDataset("virtual-predicates-example.ttl"));
        Dataset dataset = RDFDataMgr.loadDataset("virtual-predicates-example.ttl");
        QueryExecutionFactoryQuery qef = new QueryExecutionFactoryDataset(dataset);

        LookupService<Node, Table> ls = new LookupServiceSparqlQuery(
                qef,
                QueryFactory.create("SELECT * { ?s ?p ?o }"),
                Vars.s);

        Flowable<Entry<Node, Table>> flowable = ls.apply(Arrays.asList(
            NodeFactory.createURI("http://www.example.org/Anne"),
            NodeFactory.createURI("http://www.example.org/Bob")
        ));


        flowable.subscribe(item -> System.out.println("Item: " + item));
    }

    @Test(expected=RuntimeException.class)
    public void testSelectListSimple() {
        Dataset dataset = RDFDataMgr.loadDataset("virtual-predicates-example.ttl");

        DelayerDefault delayer = new DelayerDefault(5000);
        delayer.setLastRequestTime(System.currentTimeMillis());

        MapService<Fragment1, Node, Table> ms = new MapServiceSparqlQuery(
                new QueryExecutionFactoryDelay(new QueryExecutionFactoryDataset(dataset), delayer),
                QueryFactory.create("SELECT * { ?s ?p ?o }"),
                Vars.s);

        Flowable<Entry<Node, Table>> flowable = ms.createPaginator(ConceptUtils.createSubjectConcept()).apply(Range.all());

        flowable
            .timeout(1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .toList().blockingGet();
        //flowable.take(1).subscribe(item -> System.out.println("Item: " + item));
    }


    @Test
    public void testSelectCancelListSimple() {
        Dataset dataset = RDFDataMgr.loadDataset("virtual-predicates-example.ttl");

        DelayerDefault delayer = new DelayerDefault(5000);
        delayer.setLastRequestTime(System.currentTimeMillis());

        MapService<Fragment1, Node, Table> ms = new MapServiceSparqlQuery(
                new QueryExecutionFactoryDelay(new QueryExecutionFactoryDataset(dataset), delayer),
                QueryFactory.create("SELECT * { ?s ?p ?o }"),
                Vars.s);


        boolean[] isCancelled = {false};
        Iterator<Entry<Node, Table>> it =
                ms.createPaginator(ConceptUtils.createSubjectConcept()).apply(Range.all())
                .takeWhile(x -> {
                    boolean r = !isCancelled[0];
                    System.out.println("isCancelled = " + !r);
                    return r;
                })
                .blockingIterable().iterator();

        System.out.println("next()");
        it.next();
        System.out.println("cancelling");
        isCancelled[0] = true;
        System.out.println("next()");
        it.next();
        //flowable.take(1).subscribe(item -> System.out.println("Item: " + item));
    }

}
