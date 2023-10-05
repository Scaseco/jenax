package org.aksw.jena_sparql_api.sparql.ext.util;

import java.util.ArrayList;
import java.util.List;

import org.aksw.commons.collections.IteratorUtils;
import org.aksw.jenax.arq.util.exec.query.QueryExecSimple;
import org.aksw.jenax.norse.NorseTerms;
import org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.sse.SSE;

import com.google.common.collect.Iterables;

public class MoreQueryExecUtils {
    public static PrefixMapping createTestPrefixMapping() {
        PrefixMapping result = new PrefixMappingImpl();
        result.setNsPrefixes(SSE.getPrefixMapRead()); // afn, and such (not the right apf though!)
        result.setNsPrefixes(GeoSPARQL_URI.getPrefixes());
        result.setNsPrefix("norse", NorseTerms.NS);
        JenaExtensionUtil.addPrefixes(result);

        return result;
    }

    public static final QueryExecSimple INSTANCE = QueryExecSimple.create(createTestPrefixMapping());


    public static Node evalToNode(QueryExec qeTmp) {
        Query query = qeTmp.getQuery();
        Var resultVar = Iterables.getOnlyElement(query.getProjectVars());
        Binding binding = evalToBinding(qeTmp);
        Node result = binding == null ? null : binding.get(resultVar);
        return result;
    }

    public static Binding evalToBinding(QueryExec qeTmp) {
        Binding result;
        try (QueryExec qe = qeTmp) {
            RowSet rs = qe.select();
            result = IteratorUtils.expectZeroOrOneItems(rs);
        }
        return result;
    }

    // Project a certain column into a list of nodes
    public static List<Node> evalToNodes(QueryExec qeTmp) {
        Query query = qeTmp.getQuery();
        Var resultVar = Iterables.getOnlyElement(query.getProjectVars());
        List<Node> result = new ArrayList<>();
        try (QueryExec qe = qeTmp) {
            qe.select().forEachRemaining(b -> result.add(b.get(resultVar)));
        }
        return result;
    }

    public static String evalToLexicalForm(QueryExec qe) {
        Node node = evalToNode(qe);
        String result = node == null ? null :
            node.isLiteral() ? node.getLiteralLexicalForm() : node.toString();
        return result;
    }
}
