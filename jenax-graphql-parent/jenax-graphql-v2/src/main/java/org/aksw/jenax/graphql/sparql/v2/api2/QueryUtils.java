package org.aksw.jenax.graphql.sparql.v2.api2;

import org.aksw.jenax.graphql.sparql.v2.util.ElementUtils;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementSubQuery;

public class QueryUtils {
    public static Query elementToQuery(Element pattern) {
        return elementToQuery(pattern, null);
    }

    public static Query elementToQuery(Element pattern, String resultVar) {
        if (pattern == null)
            return null;
        Query query = new Query();

        Element cleanElement = pattern instanceof ElementGroup || pattern instanceof ElementSubQuery
                ? pattern
                : ElementUtils.createElementGroup(pattern);

        query.setQueryPattern(cleanElement);
        query.setQuerySelectType();

        if (resultVar == null) {
            query.setQueryResultStar(true);
        }

        query.setResultVars();

        if (resultVar != null) {
            query.getResultVars().add(resultVar);
        }

        return query;
    }
}
