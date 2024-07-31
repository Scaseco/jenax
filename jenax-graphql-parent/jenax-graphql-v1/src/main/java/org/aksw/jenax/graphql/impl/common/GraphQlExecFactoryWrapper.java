package org.aksw.jenax.graphql.impl.common;

import java.util.Map;

import org.aksw.jenax.graphql.json.api.GraphQlExec;
import org.aksw.jenax.graphql.json.api.GraphQlExecFactory;

import graphql.language.Document;
import graphql.language.Value;

public interface GraphQlExecFactoryWrapper
    extends GraphQlExecFactory
{
    GraphQlExecFactory getDelegate();

    @Override
    default GraphQlExec create(Document document, Map<String, Value<?>> assignments) {
        return getDelegate().create(document, assignments);
    }

    @Override
    default GraphQlExec create(String documentString, Map<String, Value<?>> assignments) {
        return getDelegate().create(documentString, assignments);
    }
}
