package org.aksw.jenax.graphql.sparql;

import org.aksw.jenax.graphql.rdf.api.RdfGraphQlExec;

import graphql.language.Document;
import graphql.parser.Parser;

public abstract class RdfGraphQlExecBuilderFront
    extends RdfGraphQlExecBuilderBase
{
    protected Parser parser;

    protected RdfGraphQlExecBuilderFront() {
        this(new Parser());
    }

    protected RdfGraphQlExecBuilderFront(Parser parser) {
        super();
        this.parser = parser;
    }

    public abstract RdfGraphQlExec buildActual(Document document);

    public Document getParsedDocument() {
        Document result = document != null
                ? document
                : documentString != null
                    ? parser.parseDocument(documentString)
                    : null;
        return result;
    }

    @Override
    public final RdfGraphQlExec build() {
        Document doc = getParsedDocument();
        RdfGraphQlExec result = buildActual(doc);
        return result;
    }
}
