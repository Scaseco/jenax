package org.aksw.jenax.arq.engine.quad.assembler;

import java.io.StringReader;

import org.aksw.jenax.arq.util.exec.query.ExecutionContextUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.engine.main.QueryEngineMainQuad;
import org.apache.jena.sparql.engine.main.solver.OpExecutorQuads;
import org.apache.jena.sparql.util.Context;
import org.junit.Assert;
import org.junit.Test;

public class TestDatasetAssemblerQuads {

    private static final String SPEC_STR_01 = String.join("\n",
        "PREFIX ja: <http://jena.hpl.hp.com/2005/11/Assembler#>",
        "<urn:example:root> a ja:DatasetQuads ; ja:dataset <urn:example:base> .",
        "<urn:example:base> a ja:MemoryDataset ."
    );

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
}
