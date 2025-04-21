package org.aksw.jenax.graphql.sparql.v2.util;

import java.util.function.Supplier;

public class ObjectUtils {
    public static Class<?> getClass(Object o) {
        return o == null ? null : o.getClass();
    }

    public static <T> T createIfNull(T value, Supplier<T> supplier) {
        return value == null ? supplier.get() : value;
    }
}
