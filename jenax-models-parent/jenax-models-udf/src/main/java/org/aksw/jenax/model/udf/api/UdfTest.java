package org.aksw.jenax.model.udf.api;

import java.io.ByteArrayInputStream;

import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

public class UdfTest {
    public static void main(String[] args) {
        JenaPluginUtils.scan(UdfTest.class);

        String tmp = "<http://www.example.org/myfn>\n" +
                "  <https://w3id.org/aksw/norse#udf.simpleDefinition> (\"<http://jena.apache.org/ARQ/function#bnode>(?x)\" \"x\") ;\n" +
                "  .";

        Model m = ModelFactory.createDefaultModel();
        RDFDataMgr.read(m, new ByteArrayInputStream(tmp.getBytes()), Lang.TURTLE);

        UserDefinedFunctionResource d = m.createResource("http://www.example.org/myfn").as(UserDefinedFunctionResource.class);
//		//UserDefinedFunctionDefinition udfd = d.toJena();
//		System.out.println(udfd.getUri());
//		System.out.println(udfd.getBaseExpr());
//		System.out.println(udfd.getArgList());
//		System.out.println(d.getDefinitions().stream().filter(x -> x.mapsToPropertyFunction()).findAny());
    }

}
