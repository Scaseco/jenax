package org.apache.jena.fuseki.mod.graphql;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class IO2 {
    public static byte[] readResourceAsBytes(Class<?> clz, String name) throws IOException {
        ClassLoader classLoader = clz.getClassLoader();
        return readResourceAsBytes(classLoader, name);
    }

    public static byte[] readResourceAsBytes(ClassLoader classLoader, String name) throws IOException {
        byte[] result;
        try (InputStream in = classLoader.getResourceAsStream(name)) {
            result = IOUtils.toByteArray(in);
        }
        return result;
    }
}
