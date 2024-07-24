package org.aksw.jenax.graphql.impl.common;

import java.util.Map;
import java.util.Objects;

import org.aksw.jenax.graphql.json.api.GraphQlExec;
import org.aksw.jenax.graphql.json.api.GraphQlExecFactory;
import org.aksw.jenax.graphql.json.api.GraphQlExecFactoryDocument;

import graphql.language.Document;
import graphql.language.Value;
import graphql.parser.Parser;

/** Front end implementation. Bundles the core logic (based on
 * {@link Document}) with a method for dealing with strings. */
//public class GraphQlExecFactoryFront
//    extends GraphQlExecFactoryDocumentWrapperBase
//    implements GraphQlExecFactory
//{
//    protected Parser parser;
//
//    protected GraphQlExecFactoryFront(GraphQlExecFactoryDocument delegate, Parser parser) {
//        super(delegate);
//        this.parser = parser;
//    }
//
//    @Override
//    public GraphQlExec create(String documentStr, Map<String, Value<?>> assignments) {
//        Document document = parser.parseDocument(documentStr);
//        return create(document, assignments);
//    }
//
//    public static GraphQlExecFactoryFront of(GraphQlExecFactoryDocument delegate) {
//        return of(delegate, new Parser());
//    }
//
//    public static GraphQlExecFactoryFront of(GraphQlExecFactoryDocument delegate, Parser parser) {
//        Objects.requireNonNull(delegate);
//        Objects.requireNonNull(parser);
//        return new GraphQlExecFactoryFront(delegate, parser);
//    }
//}
