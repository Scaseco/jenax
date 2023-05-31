package org.aksw.jenax.arq.util.exec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.StageBuilder;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryExecutionUtils {
    private static final Logger logger = LoggerFactory.getLogger(QueryExecutionUtils.class);

    public static Integer fetchInteger(QueryExecution qe, Var v) {
        return Optional.ofNullable(fetchNumber(qe, v)).map(Number::intValue).orElse(null);
    }

    public static Long fetchLong(QueryExecution qe, Var v) {
        return Optional.ofNullable(fetchNumber(qe, v)).map(Number::longValue).orElse(null);
    }

    /**
     * Attempt to get a Number from the first row of a result set for a given variable.
     * Returns null if there is no row or the value is unbound.
     * Raises an exception if the obtained RDFNode cannot be converted to a number or if there is more than 1 result row.
     *
     * @param qe
     * @param v
     * @return
     */
    public static Number fetchNumber(QueryExecution qe, Var v) {
        Number result;

        RDFNode tmp = QueryExecUtils.getAtMostOne(qe, v.getName());
        if(tmp != null && tmp.isLiteral()) {
            Object val = tmp.asLiteral().getValue();
            if(val == null) {
                result = null;
            } else {
                if (!(val instanceof Number)) {
                    throw new RuntimeException("Value " + val + " is not a Number");
                }

                result = (Number)val;
            }
        } else {
            throw new RuntimeException("RDFNode " + tmp + " is not a literal");
        }

        return result;
    }

    public static List<Node> executeList(Function<? super Query, ? extends QueryExecution> qef, Query query) {
        Var var = QueryUtils.extractSoleProjectVar(query);

        List<Node> result = executeList(qef, query, var);
        return result;
    }

    public static List<Node> executeList(Function<? super Query, ? extends QueryExecution> qef, Query query, Var var) {
        List<Node> result = new ArrayList<Node>();

        try (QueryExecution qe = qef.apply(query)) {
            ResultSet rs = qe.execSelect();
            while(rs.hasNext()) {
                Binding binding = rs.nextBinding();
                Node node = binding.get(var);

                result.add(node);
            }
        }

        return result;
    }

    public static List<Binding> execListBinding(Function<? super Query, ? extends QueryExecution> qef, Query query) {
        List<Binding> result = new ArrayList<>();
        try (QueryExecution qe = qef.apply(query)) {
            ResultSet rs = qe.execSelect();
            while(rs.hasNext()) {
                Binding binding = rs.nextBinding();
                result.add(binding);
            }
        }

        return result;
    }


    public static <T extends RDFNode> List<T> executeRdfList(Function<? super Query, ? extends QueryExecution> qef, Query query, Class<T> viewClass) {
        Var var = QueryUtils.extractSoleProjectVar(query);

        List<T> result = executeRdfList(qef, query, var.getName(), viewClass);
        return result;
    }

    public static <T extends RDFNode> List<T> executeRdfList(Function<? super Query, ? extends QueryExecution> qef, Query query, String varName, Class<T> viewClass) {
        List<T> result = new ArrayList<>();

        try (QueryExecution qe = qef.apply(query)) {
            ResultSet rs = qe.execSelect();
            while(rs.hasNext()) {
                QuerySolution qs = rs.next();
                RDFNode rdfNode = qs.get(varName);

                T item = rdfNode.as(viewClass);
                result.add(item);
            }
        }

        return result;
    }

    public static boolean wrapWithAutoDisableReorder(Query query, Context cxt) {
        boolean result = false;
        if (query == null) {
            logger.warn("Could not obtain query from query execution.");
        } else if (cxt != null) {

            boolean disableTpReorder = QueryUtils.shouldDisablePatternReorder(query);
            if (disableTpReorder) {
                logger.info("Pattern reorder disabled due to presence of property functions and/or service clauses");
            }

            if (disableTpReorder) {
                StageBuilder.setGenerator(cxt, StageBuilder.executeInline);
                result = true;
            }
        }
        return result;
    }
}
