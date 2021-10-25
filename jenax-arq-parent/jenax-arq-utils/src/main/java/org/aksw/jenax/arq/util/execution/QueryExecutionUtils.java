package org.aksw.jenax.arq.util.execution;

import java.util.Optional;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.util.QueryExecUtils;

public class QueryExecutionUtils {
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

}
