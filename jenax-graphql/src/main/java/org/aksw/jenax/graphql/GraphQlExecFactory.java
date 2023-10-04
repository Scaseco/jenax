package org.aksw.jenax.graphql;

import graphql.language.Document;

public interface GraphQlExecFactory {
    GraphQlExec create(Document document);
}
