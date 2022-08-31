package org.aksw.jena_sparql_api.sparql.ext.util;

import java.util.ArrayList;
import java.util.List;

import org.aksw.commons.collections.IteratorUtils;
import org.apache.curator.shaded.com.google.common.collect.Iterables;
import org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI;
import org.apache.jena.graph.Node;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sparql.util.QueryExecUtils;

public class MoreQueryExecUtils {
    public static PrefixMapping createTestPrefixMapping() {
        PrefixMapping result = new PrefixMappingImpl();
        result.setNsPrefixes(SSE.getPrefixMapRead()); // afn, and such (not the right apf though!)
        result.setNsPrefixes(GeoSPARQL_URI.getPrefixes());
        JenaExtensionUtil.addPrefixes(result);

        return result;
    }

    public static Node evalExprToNode(String exprStr) {
        PrefixMapping pm = createTestPrefixMapping();
        Expr expr = ExprUtils.parse(exprStr, pm);
        // Must never return null; instead an ExprEvalException must be raised
        NodeValue nv = ExprUtils.eval(expr);
        Node result = nv.asNode();
        return result;
    }

    public static String evalExprToLexicalForm(String exprStr) {
        Node node = evalExprToNode(exprStr);
        String result = node == null ? null : node.getLiteralLexicalForm();
        return result;
    }

    public static Node evalQueryToNode(String queryStr) {
        Query query = new Query();
        query.getPrefixMapping().setNsPrefixes(createTestPrefixMapping());
        QueryFactory.parse(query, queryStr, null, Syntax.syntaxARQ);
        RDFNode rdfNode = QueryExecUtils.getExactlyOne(query.toString(), DatasetFactory.empty());

        Node result = rdfNode == null ? null : rdfNode.asNode();
        return result;
    }

    public static Binding evalQueryToBinding(String queryStr) {
        Query query = new Query();
        query.getPrefixMapping().setNsPrefixes(createTestPrefixMapping());
        QueryFactory.parse(query, queryStr, null, Syntax.syntaxARQ);
        Binding result = null;
        try (QueryExecution qe = QueryExecutionFactory.create(query, DatasetFactory.empty())) {
            RowSet rs = RowSet.adapt(qe.execSelect());
            result = IteratorUtils.expectZeroOrOneItems(rs);
        }
        return result;
    }

    public static List<Node> evalQueryToNodes(String queryStr) {
        Query query = new Query();
        query.getPrefixMapping().setNsPrefixes(createTestPrefixMapping());
        QueryFactory.parse(query, queryStr, null, Syntax.syntaxARQ);
        Var resultVar = Iterables.getOnlyElement(query.getProjectVars());

        List<Node> result = new ArrayList<>();
        try (QueryExecution qe = QueryExecutionFactory.create(query, DatasetFactory.empty())) {
            RowSet.adapt(qe.execSelect())
                .forEachRemaining(b -> result.add(b.get(resultVar)));
        }

        return result;
    }

    public static String evalQueryToLexicalForm(String queryStr) {
        Node node = evalQueryToNode(queryStr);
        String result = node == null ? null :
            node.isLiteral() ? node.getLiteralLexicalForm() : node.toString();
        return result;
    }
}
