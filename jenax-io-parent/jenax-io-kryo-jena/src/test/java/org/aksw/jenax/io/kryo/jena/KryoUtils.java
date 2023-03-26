package org.aksw.jenax.io.kryo.jena;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.BiConsumer;

import org.junit.Assert;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KryoUtils {
    public static <T> void testRoundtrip(Kryo kryo, T expected) {
        testRoundtrip(kryo, expected, (_expected, actual) -> {
            Assert.assertEquals(_expected, actual);
        });
    }

    public static <T> void testRoundtrip(Kryo kryo, T expected, BiConsumer<T, T> asserter) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Output out = new Output(baos)) {
            kryo.writeClassAndObject(out, expected);
            out.flush();
            try (ByteArrayInputStream bain = new ByteArrayInputStream(baos.toByteArray());
                    Input in = new Input(bain)) {
                @SuppressWarnings("unchecked")
                T actual = (T)kryo.readClassAndObject(in);
                asserter.accept(expected, actual);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
