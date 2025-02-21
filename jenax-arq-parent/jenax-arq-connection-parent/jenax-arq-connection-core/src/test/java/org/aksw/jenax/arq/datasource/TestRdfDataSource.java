package org.aksw.jenax.arq.datasource;

import java.util.List;

import org.aksw.jenax.arq.util.op.RewriteList;
import org.aksw.jenax.dataaccess.deleted.RDFLinkSourceWrapperWithRewrite;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSources;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.junit.Assert;
import org.junit.Test;


public class TestRdfDataSource {
    @Test
    public void testWrapping() {
        RDFDataSource core = RdfDataSources.of(DatasetGraphFactory.empty());
        core = RdfDataSources.decorate(core, (RDFDataSource ds) -> RdfDataSources.decorate(ds, (Op op) -> op));
        core = RdfDataSources.decorate(core, (RDFDataSource ds) -> RdfDataSources.decorate(ds, (Op op) -> op));

        RDFLinkSourceWrapperWithRewrite<?> wrapper = (RDFLinkSourceWrapperWithRewrite<?>)core.asLinkSource();
        RewriteList rewrite = (RewriteList)wrapper.getRewrite();
        List<Rewrite> rewrites = rewrite.getRewrites();
        Assert.assertEquals(2, rewrites.size());
    }
}
