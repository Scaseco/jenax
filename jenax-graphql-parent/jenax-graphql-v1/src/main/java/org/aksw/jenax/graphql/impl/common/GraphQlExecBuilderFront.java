package org.aksw.jenax.graphql.impl.common;

import org.aksw.jenax.graphql.json.api.GraphQlExec;

import graphql.language.Document;
import graphql.parser.Parser;

public abstract class GraphQlExecBuilderFront
    extends GraphQlExecBuilderBase
{
    protected Parser parser;

    protected GraphQlExecBuilderFront() {
        this(new Parser());
    }

    protected GraphQlExecBuilderFront(Parser parser) {
        super();
        this.parser = parser;
    }

    public abstract GraphQlExec buildActual(Document document);

    public Document getParsedDocument() {
        Document result = document != null
                ? document
                : documentString != null
                    ? parser.parseDocument(documentString)
                    : null;
        return result;
    }

    @Override
    public final GraphQlExec build() {
        Document doc = getParsedDocument();
        GraphQlExec result = buildActual(doc);
        return result;
    }
}
