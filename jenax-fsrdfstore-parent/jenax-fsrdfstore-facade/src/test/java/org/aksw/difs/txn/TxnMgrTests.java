package org.aksw.difs.txn;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;

import org.aksw.commons.path.core.PathStr;
import org.aksw.commons.txn.api.Txn;
import org.aksw.commons.txn.api.TxnMgr;
import org.aksw.commons.txn.api.TxnResourceApi;
import org.aksw.difs.builder.DifsFactory;
import org.aksw.jena_sparql_api.difs.main.DatasetGraphFromTxnMgr;
import org.aksw.jena_sparql_api.difs.txn.TxnUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.vocabulary.RDF;
import org.jgrapht.GraphPath;
import org.junit.Assert;
import org.junit.Test;

public class TxnMgrTests {

    /**
     * This test creates a deadlock situation where two transactions
     * each lock one of two resources and then attempt to lock the other resource as well.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void detectDeadLockTest() throws IOException, InterruptedException {
        TxnMgr txnMgr = DifsFactory.newInstance()
            .setCreateIfNotExists(true)
            .setConfigFile(Paths.get("/tmp/store.conf.ttl"))
            .setStoreDefinition(sd -> sd.setHeartbeatInterval(5000l))
            .createTxnMgr();

        try {
            Txn a = txnMgr.newTxn("txn-a", true, true);
            Txn b = txnMgr.newTxn("txn-b", true, true);

            TxnResourceApi r1a = a.getResourceApi(PathStr.newRelativePath("r1"));
            r1a.declareAccess();
            r1a.getTxnResourceLock().writeLock().lock();

            TxnResourceApi r2b = b.getResourceApi(PathStr.newRelativePath("r2"));
            r2b.declareAccess();
            r2b.getTxnResourceLock().writeLock().lock();

            TxnResourceApi r2a = a.getResourceApi(PathStr.newRelativePath("r2"));
            r2a.declareAccess();
            //r2a.getTxnResourceLock().writeLock().tryLock(1, TimeUnit.SECONDS);

            TxnResourceApi r1b = b.getResourceApi(PathStr.newRelativePath("r1"));
            r1b.declareAccess();
    //		r1b.getTxnResourceLock().writeLock().lock();

            Set<GraphPath<Node, Triple>> cycles = TxnUtils.detectDeadLocksRaw(txnMgr);
            Assert.assertEquals(1, cycles.size());
        } finally {
            txnMgr.deleteResources();
        }
    }

    @Test
    public void rollbackStaleTxnTest() throws IOException, InterruptedException {
        DatasetGraph dg = DifsFactory.newInstance()
                .setCreateIfNotExists(true)
                .setConfigFile(Paths.get("/tmp/store.conf.ttl"))
                .setStoreDefinition(sd -> {
                    sd.setHeartbeatInterval(1l);
                })
                .connect();

        try {
            dg.begin(ReadWrite.WRITE);
            dg.add(RDF.Nodes.type, RDF.Nodes.type, RDF.Nodes.type, RDF.Nodes.type);

            Thread.sleep(100);
            // Starting another txn mgr on the same resources after some delay should
            // roll back the prior transaction
            DatasetGraph dg2 = DifsFactory.newInstance()
                    .setConfigFile(Paths.get("/tmp/store.conf.ttl"))
                    .connect();


        } finally {
            ((DatasetGraphFromTxnMgr)dg).getTxnMgr().deleteResources();
        }
    }

    @Test
    public void detectStaleTxnTest() throws IOException, InterruptedException {
        TxnMgr txnMgr = DifsFactory.newInstance()
                .setCreateIfNotExists(true)
                .setConfigFile(Paths.get("/tmp/store.conf.ttl"))
                .setStoreDefinition(sd -> sd.setHeartbeatInterval(1l))
                .createTxnMgr();

        try {
            Txn txn = txnMgr.newTxn(true, true);
            Thread.sleep(10);

            boolean isStale = txn.isStale();
            Assert.assertTrue(isStale);
        } finally {
            txnMgr.deleteResources();
        }
    }

}
