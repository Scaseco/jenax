package org.aksw.jenax.sparql.ext.mvn;

import org.aksw.jena_sparql_api.sparql.ext.util.MoreQueryExecUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.junit.Assert;
import org.junit.Test;

public class TestSparqlLibMaven {
    @Test
    public void testMvnIri() {
        Binding actual = MoreQueryExecUtils.INSTANCE.evalQueryToBinding("SELECT (norse:mvn.toPath(<urn:mvn:org.aksw.jenax:jenax-arq-plugin-bundle:1.0.0>) AS ?x) {}");
        Binding expected = BindingFactory.binding(Vars.x, NodeFactory.createLiteralString("internal/org/aksw/jenax/jenax-arq-plugin-bundle/1.0.0/jenax-arq-plugin-bundle-1.0.0"));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testMvnStr01() {
        Binding actual = MoreQueryExecUtils.INSTANCE.evalQueryToBinding("SELECT (norse:mvn.toPath('urn:mvn:org.aksw.jenax:jenax-arq-plugin-bundle:1.0.0') AS ?x) {}");
        Binding expected = BindingFactory.binding(Vars.x, NodeFactory.createLiteralString("internal/org/aksw/jenax/jenax-arq-plugin-bundle/1.0.0/jenax-arq-plugin-bundle-1.0.0"));
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testMvnStr02() {
        Binding actual = MoreQueryExecUtils.INSTANCE.evalQueryToBinding("SELECT (norse:mvn.toPath('org.aksw.jenax:jenax-arq-plugin-bundle:1.0.0') AS ?x) {}");
        Binding expected = BindingFactory.binding(Vars.x, NodeFactory.createLiteralString("internal/org/aksw/jenax/jenax-arq-plugin-bundle/1.0.0/jenax-arq-plugin-bundle-1.0.0"));
        Assert.assertEquals(expected, actual);
    }
}
