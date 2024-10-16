package org.aksw.jenax.graphql.sparql.v2.context;

import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.lang3.ClassUtils;

import graphql.language.Directive;

public class DirectiveParserImpl<T>
    implements DirectiveParser<T>
{
    protected final Class<? super T> javaClass;
    protected final String name;
    protected final boolean isUnique;
    protected Function<Directive, T> parser;

    protected DirectiveParserImpl(Class<? super T> javaClass, String name, boolean isUnique, Function<Directive, T> parser) {
        super();
        this.javaClass = Objects.requireNonNull(javaClass);
        this.name = Objects.requireNonNull(name);
        this.isUnique = isUnique;
        this.parser = Objects.requireNonNull(parser);
    }

    public static <T> DirectiveParser<T> of(Class<? super T> javaClass, String name, boolean isUnqiue, Function<Directive, T> parser) {
        return new DirectiveParserImpl<>(javaClass, name, isUnqiue, parser);
    }

    public Class<? super T> getJavaClass() {
        return javaClass;
    }

    public boolean supports(Class<?> interfaceToTestFor) {
        boolean result = ClassUtils.getAllInterfaces(javaClass).contains(interfaceToTestFor);
        return result;
    }

    @Override
    public String getName() {
        return name;
    }
    @Override
    public boolean isUnique() {
        return isUnique;
    }

    @Override
    public T parser(Directive directive) {
        return parser.apply(directive);
    }
}
