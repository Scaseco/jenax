package org.aksw.jenax.arq.datasource;

import java.util.List;

import org.aksw.jenax.arq.util.op.RewriteList;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceWrapperWithRewrite;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSources;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.junit.Assert;
import org.junit.Test;


public class TestRdfDataSource {
    @Test
    public void testWrapping() {
        RdfDataSource core = RdfDataSources.of(DatasetGraphFactory.empty());
        core = core.decorate(ds -> RdfDataSources.wrapWithOpTransform(ds, op -> op));
        core = core.decorate(ds -> RdfDataSources.wrapWithOpTransform(ds, op -> op));

        RdfDataSourceWrapperWithRewrite<?> wrapper = (RdfDataSourceWrapperWithRewrite<?>)core;
        RewriteList rewrite = (RewriteList)wrapper.getRewrite();
        List<Rewrite> rewrites = rewrite.getRewrites();
        Assert.assertEquals(2, rewrites.size());
    }
}
