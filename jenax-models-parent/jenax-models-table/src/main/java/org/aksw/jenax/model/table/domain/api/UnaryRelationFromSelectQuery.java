package org.aksw.jenax.model.table.domain.api;

import java.util.Objects;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

public interface UnaryRelationFromSelectQuery
    extends UnaryRelationDef
{
    @IriNs("eg")
    void setQueryString(String queryString);
    String getQueryString();

    default Query getQuery() {
        String queryString = getQueryString();
        Objects.requireNonNull(queryString);
        Query result = QueryFactory.create(queryString);
        return result;
    }

//    default UnaryRelation getRelation() {
//        Query query = getQuery();
//        Object result = null; // TODO Concept.of(query);
//        return result;
//    }
}
