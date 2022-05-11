package org.aksw.jena_sparql_api.sparql.ext.xml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.jenax.arq.util.var.Vars;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Resources;


public class TestSparqlXPath {
	@Test
	public void testSparqlXPath() throws IOException, URISyntaxException {
		URL url = Resources.getResource("sparql-ext-test-xml-01.sparql");
		String text = Resources.toString(url, StandardCharsets.UTF_8);
	
		Query query = QueryFactory.create(text);
		Op op = Algebra.compile(query);
//		System.out.println(op);
		
		List<String> actual = new ArrayList<>();
		try(RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.create())) {
			conn.querySelect(query, b -> actual.add(b.get("str").toString()));
//			try(QueryExecution qe = conn.query(query)) {
//				System.out.println(ResultSetFormatter.asText(qe.execSelect()));
//			}
			//Model result = conn.queryConstruct(text);
			//RDFDataMgr.write();
		}
		
		Assert.assertEquals(Arrays.asList("1", "2"), actual);
	}
	
	
	@Test
	public void testSparqlXPath2() throws IOException, URISyntaxException {
		URL url = Resources.getResource("sparql-ext-test-xml-02.xml");
		String text = Resources.toString(url, StandardCharsets.UTF_8);

		Binding b = BindingFactory.binding(Vars.x, NodeFactoryExtra.createLiteralNode(text, null, RDFDatatypeXml.IRI));
		
		Query query = QueryFactory.create(String.join("\n",
				"PREFIX xml: <http://jsa.aksw.org/fn/xml/> SELECT ?dim ?isFoobarBound ?lowerCorner {",
				"  ?x xml:unnest ('//gml:Envelope' ?env) ",
				"  BIND(xml:path(?env, '@srsDimension') AS ?dim)",
				"  BIND(xml:path(?env, '@foobar') AS ?foobar)",
				"  BIND(BOUND(?foobar) AS ?isFoobarBound)",
				"  BIND(xml:path(?env, '//gml:lowerCorner') AS ?lowerCorner)",
				"}"));
		try (QueryExec qe = QueryExec.newBuilder()
				.initialBinding(b)
				.dataset(DatasetGraphFactory.create())
				.query(query)
				.build()) {
			QueryExecUtils.exec(qe);
		}
		
		/* Expected:
		---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		| dim | isFoobarBound | lowerCorner                                                                                                                                                                                       |
		===========================================================================================================================================================================================================================
		| "2" | false         | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><gml:lowerCorner xmlns:gml=\"http://www.opengis.net/gml\">-2.00375083428E7 -4.49274327583E7</gml:lowerCorner>"^^<http://www.w3.org/2001/XMLSchema#xml> |
		| "2" | false         | "<?xml version=\"1.0\" encoding=\"UTF-8\"?><gml:lowerCorner xmlns:gml=\"http://www.opengis.net/gml\">477207.2493 879265.451</gml:lowerCorner>"^^<http://www.w3.org/2001/XMLSchema#xml>            |
		---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		*/
	}
}
