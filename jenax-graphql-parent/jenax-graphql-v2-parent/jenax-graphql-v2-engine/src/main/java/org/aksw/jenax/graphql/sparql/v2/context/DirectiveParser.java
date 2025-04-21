package org.aksw.jenax.graphql.sparql.v2.context;

import graphql.language.Directive;

public interface DirectiveParser<T> {
    /** The directive which this parser parses. */
    String getName();

    /** Whether the directive is unique. */
    boolean isUnique();

    T parser(Directive directive);
}
