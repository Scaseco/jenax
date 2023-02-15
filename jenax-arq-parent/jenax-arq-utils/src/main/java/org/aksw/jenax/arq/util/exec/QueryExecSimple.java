package org.aksw.jenax.arq.util.exec;

import java.util.ArrayList;
import java.util.List;

import org.aksw.commons.collections.IteratorUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sparql.util.QueryExecUtils;

import com.google.common.collect.Iterables;

/**
 * This class is mainly intended for easing writing test cases.
 * Instances of this class can be configured with a given prefix mapping
 * for parsing queries.
 * Several methods are then provided to evaluate a query and extract a specific
 * value from the result set.
 */
public class QueryExecSimple {

    protected PrefixMapping prefixMapping;

    protected QueryExecSimple(PrefixMapping prefixMapping) {
        super();
        this.prefixMapping = prefixMapping;
    }

    public static QueryExecSimple create(PrefixMapping prefixMapping) {
        return new QueryExecSimple(prefixMapping);
    }

    public String evalExprToLexicalForm(String exprStr) {
        Node node = evalExprToNode(exprStr);
        String result = node == null ? null : node.getLiteralLexicalForm();
        return result;
    }

    public Node evalExprToNode(String exprStr) {
        Expr expr = ExprUtils.parse(exprStr, prefixMapping);
        // Must never return null; instead an ExprEvalException must be raised
        NodeValue nv = ExprUtils.eval(expr);
        Node result = nv.asNode();
        return result;
    }

    public Node evalQueryToNode(String queryStr) {
        Query query = new Query();
        query.getPrefixMapping().setNsPrefixes(prefixMapping);
        QueryFactory.parse(query, queryStr, null, Syntax.syntaxARQ);
        RDFNode rdfNode = QueryExecUtils.getExactlyOne(query.toString(), DatasetFactory.empty());

        Node result = rdfNode == null ? null : rdfNode.asNode();
        return result;
    }

    public Binding evalQueryToBinding(String queryStr) {
        Query query = new Query();
        query.getPrefixMapping().setNsPrefixes(prefixMapping);
        QueryFactory.parse(query, queryStr, null, Syntax.syntaxARQ);
        Binding result = null;
        try (QueryExecution qe = QueryExecutionFactory.create(query, DatasetFactory.empty())) {
            RowSet rs = RowSet.adapt(qe.execSelect());
            result = IteratorUtils.expectZeroOrOneItems(rs);
        }
        return result;
    }

    public List<Node> evalQueryToNodes(String queryStr) {
        Query query = new Query();
        query.getPrefixMapping().setNsPrefixes(prefixMapping);
        QueryFactory.parse(query, queryStr, null, Syntax.syntaxARQ);
        Var resultVar = Iterables.getOnlyElement(query.getProjectVars());

        List<Node> result = new ArrayList<>();
        try (QueryExecution qe = QueryExecutionFactory.create(query, DatasetFactory.empty())) {
            RowSet.adapt(qe.execSelect())
                .forEachRemaining(b -> result.add(b.get(resultVar)));
        }

        return result;
    }

    public String evalQueryToLexicalForm(String queryStr) {
        Node node = evalQueryToNode(queryStr);
        String result = node == null ? null :
            node.isLiteral() ? node.getLiteralLexicalForm() : node.toString();
        return result;
    }
}
