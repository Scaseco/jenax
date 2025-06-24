package org.aksw.jenax.graphql.sparql.v2.context;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class DirectiveParseRegistry {
    private static DirectiveParseRegistry INSTANCE;

    public static DirectiveParseRegistry get() {
        if (INSTANCE == null) {
            synchronized (DirectiveParseRegistry.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DirectiveParseRegistry();
                }
            }
        }
        return INSTANCE;
    }

    protected Map<String, DirectiveParser<?>> registry;

    public DirectiveParseRegistry() {
        super();
        this.registry = new LinkedHashMap<>();
    }

    public <T> void put(DirectiveParser<T> parser) {
        String name = parser.getName();
        registry.put(name, parser);
    }

    public <T> DirectiveParser<T> get(String name) {
        DirectiveParser<?> tmp = registry.get(name);
        if (tmp == null) {
            throw new NoSuchElementException("No parser for name: " + name);
        }

        @SuppressWarnings("unchecked")
        DirectiveParser<T> result = (DirectiveParser<T>)tmp;
        return result;
    }
}
