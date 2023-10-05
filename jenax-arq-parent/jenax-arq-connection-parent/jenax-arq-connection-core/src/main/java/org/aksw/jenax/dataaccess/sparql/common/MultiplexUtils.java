package org.aksw.jenax.dataaccess.sparql.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class MultiplexUtils {

    public static <T> void forEach(Collection<? extends T> items, Consumer<? super T> handler) {
        List<Throwable> throwables = new ArrayList<>();
        for(T item : items) {
            try {
                handler.accept(item);
            } catch(Exception e) {
                throwables.add(e);
            }
        }

        // TODO Throw a multi exception
        if(!throwables.isEmpty()) {
            throw new RuntimeException(throwables.iterator().next());
        }
    }

    public static <T, X> X forEachAndReturnFirst(Collection<? extends T> items, Function<? super T, X> handler) {
        List<Throwable> throwables = new ArrayList<>();
        X result = null;
        boolean isFirst = true;
        for(T item : items) {
            try {
                X tmp = handler.apply(item);
                if(isFirst) {
                    result = tmp;
                    isFirst = false;
                }
            } catch(Exception e) {
                throwables.add(e);
            }
        }

        // TODO Throw a multi exception
        if(!throwables.isEmpty()) {
            throw new RuntimeException(throwables.iterator().next());
        }
        return result;
    }
}
