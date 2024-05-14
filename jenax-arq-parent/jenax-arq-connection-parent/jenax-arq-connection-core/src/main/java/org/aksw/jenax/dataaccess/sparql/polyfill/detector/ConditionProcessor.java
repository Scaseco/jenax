package org.aksw.jenax.dataaccess.sparql.polyfill.detector;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jenax.model.polyfill.domain.api.PolyfillCondition;
import org.aksw.jenax.model.polyfill.domain.api.PolyfillConditionConjunction;
import org.aksw.jenax.model.polyfill.domain.api.PolyfillConditionQuery;
import org.aksw.jenax.model.polyfill.domain.api.PolyfillConditionVisitor;
import org.aksw.jenax.stmt.core.SparqlStmtQuery;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConditionProcessor
    implements PolyfillConditionVisitor<Condition>
{
    private static final Logger logger = LoggerFactory.getLogger(ConditionProcessor.class);

    private static final ConditionProcessor INSTANCE = new ConditionProcessor();

    public static ConditionProcessor get() {
        return INSTANCE;
    }

    @Override
    public Condition visit(PolyfillConditionQuery condition) {
        String queryString = condition.getQueryString();
        Boolean isNonParseable = condition.isNonParseable();

        SparqlStmtQuery stmt;
        if (Boolean.TRUE.equals(isNonParseable)) {
            stmt = new SparqlStmtQuery(queryString);
        } else {
            Query query;
            try {
                query = QueryFactory.create(queryString);
            } catch (Exception e) {
                // System.err.println(queryString);
                // logger.error("Failed to parse query " + queryString, e);
                e.addSuppressed(new RuntimeException("Failed to parse query: " + queryString));
                throw e;
            }
            stmt = new SparqlStmtQuery(query);
        }
        Boolean matchOnNonEmptyResult = Boolean.TRUE.equals(condition.isMatchOnNonEmptyResult());

        return new ConditionQuery(stmt, matchOnNonEmptyResult);
    }

    @Override
    public Condition visit(PolyfillConditionConjunction condition) {
        List<PolyfillCondition> members = condition.getConditions();
        List<Condition> conditions = new ArrayList<>(members.size());
        for (PolyfillCondition member : members) {
            Condition contrib = member.accept(this);
            conditions.add(contrib);
        }
        return x -> conditions.stream().allMatch(c -> c.test(x));
    }
}
