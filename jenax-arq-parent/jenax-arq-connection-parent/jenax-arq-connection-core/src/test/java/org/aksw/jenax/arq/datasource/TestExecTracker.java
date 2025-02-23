package org.aksw.jenax.arq.datasource;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.aksw.jenax.dataaccess.sparql.linksource.track.DatasetGraphWithExecTracker;
import org.aksw.jenax.dataaccess.sparql.linksource.track.ExecTracker;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.system.Txn;
import org.junit.Test;

public class TestExecTracker {
    @Test
    public void test() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        IntStream.range(0, 1000).mapToObj(i -> NodeFactory.createURI("urn:foo:bar" + i))
            .forEach(x -> dsg.getDefaultGraph().add(x, x, x));

        DatasetGraph finalDsg = DatasetGraphWithExecTracker.wrap(dsg);
        ExecTracker execTracker = ExecTracker.requireTracker(dsg.getContext());

        IntStream.range(0, 1000).boxed().collect(Collectors.toCollection(ArrayList::new)).parallelStream().forEach(x -> {
//            Table actualTable = QueryExec.newBuilder()
//                    .dataset(finalDsg).query("SELECT * { ?s ?p ?o }").table();
//            System.out.println("" + x + ": " + execTracker);
            Txn.executeWrite(dsg, () -> {
                UpdateExec.dataset(finalDsg).update("INSERT { ?s ?p 1 } WHERE { ?s ?p ?o }").execute();
            });
        });
        System.out.println("Result: " + execTracker);
   }
}
