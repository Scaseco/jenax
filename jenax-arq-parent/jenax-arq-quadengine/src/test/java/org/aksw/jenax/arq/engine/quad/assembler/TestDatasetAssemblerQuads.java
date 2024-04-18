package org.aksw.jenax.arq.engine.quad.assembler;

import java.io.StringReader;
import java.util.LinkedHashSet;
import java.util.Set;

import org.aksw.jenax.arq.util.exec.query.ExecutionContextUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.engine.main.QueryEngineMainQuad;
import org.apache.jena.sparql.engine.main.solver.OpExecutorQuads;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.util.Context;
import org.junit.Assert;
import org.junit.Test;

public class TestDatasetAssemblerQuads {

    private static final String SPEC_STR_01 = String.join("\n",
        "PREFIX ja: <http://jena.hpl.hp.com/2005/11/Assembler#>",
        "<urn:example:root> a ja:DatasetQuads ; ja:dataset <urn:example:base> .",
        "<urn:example:base> a ja:MemoryDataset ."
    );

    /** Test whether the built dataset has the right OpExecutor and QueryEngineFactory */
    @Test
    public void testDatasetAssembler_01() {
        Model spec = ModelFactory.createDefaultModel();
        RDFDataMgr.read(spec, new StringReader(SPEC_STR_01), null, Lang.TURTLE);
        Dataset dataset = DatasetFactory.assemble(spec.getResource("urn:example:root"));
        Context context = dataset.getContext();

        OpExecutorFactory opExecutorFactory = QC.getFactory(context);
        Assert.assertNotNull(opExecutorFactory);

        Op op = OpTable.unit();
        DatasetGraph dsg = DatasetGraphFactory.empty();
        ExecutionContext execCxt = ExecutionContextUtils.createExecCxt(dsg);

        OpExecutor opExecutor = opExecutorFactory.create(execCxt);
        Assert.assertTrue(opExecutor instanceof OpExecutorQuads);

        QueryEngineFactory qeFactory = QueryEngineRegistry.findFactory(op, dsg, context);
        Assert.assertTrue(QueryEngineMainQuad.getFactory() == qeFactory);
    }

    /**
     * Test whether the built dataset exhibits the expected behavior:
     * SELECT * { GRAPH ?g { ?s ?p ?o } } is expected to return both the triples of the
     * union graph as well as those of the named graphs.
     */
    @Test
    public void testDatasetAssembler_02() {
        Model spec = ModelFactory.createDefaultModel();
        RDFDataMgr.read(spec, new StringReader(SPEC_STR_01), null, Lang.TURTLE);
        Dataset dataset = DatasetFactory.assemble(spec.getResource("urn:example:root"));

        Node x = NodeFactory.createURI("urn:x");
        Node y = NodeFactory.createURI("urn:y");

        Set<Quad> expected = new LinkedHashSet<>();
        expected.add(Quad.create(Quad.defaultGraphIRI, x, x, x));
        expected.add(Quad.create(y, y, y, y));

        DatasetGraph dsg = dataset.asDatasetGraph();
        dsg.getDefaultGraph().add(x, x, x);
        dsg.add(y, y, y, y);

        Set<Quad> actual = new LinkedHashSet<>();
        try (QueryExecution qe = QueryExecution.create("SELECT * { GRAPH ?g { ?s ?p ?o } }", dataset)) {
            RowSet rs = RowSet.adapt(qe.execSelect());
            try {
                while (rs.hasNext()) {
                    Binding b = rs.next();
                    Quad quad = Quad.create(b.get("g"), b.get("s"), b.get("p"), b.get("o"));
                    actual.add(quad);
                }
            } finally {
                rs.close();
            }
        }
        Assert.assertEquals(expected, actual);
    }
}
