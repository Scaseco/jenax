package org.aksw.jenax.arq.util.query;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TransformList<X, Y extends Function<X, X>>
    implements Function<X, X>
{
    protected List<Y> mods;

    public TransformList(List<Y> mods) {
        super();
        this.mods = List.copyOf(mods);
    }

    public List<Y> getMods() {
        return mods;
    }

    @Override
    public X apply(X t) {
        X result = t;
        for (Y mod : mods) {
            X next = mod.apply(result);
            result = next;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <X, Y extends Function<X, X>> Stream<Y> streamFlatten(boolean recursive, Y transform) {
        Stream<Y> result;
        if (transform instanceof TransformList) {
            result = ((TransformList<X, Y>)transform).getMods().stream();
            if (recursive) {
                result = result.flatMap(x -> streamFlatten(recursive, x));
            }
        } else {
            result = Stream.of(transform);
        }
        return result;
    }

    /**
     *
     * @param recursive true to flatten recursively, false to only flatten the first level
     * @param rewrites
     * @return
     */
    // public static <X, Y extends Function<X, X>> Y flatten(boolean recursive, Function<List<Y>, Y> ctor, Y... rewrites) {
    public static <X, Y extends Function<X, X>> Y flattenOrNull(boolean recursive, Function<List<Y>, Y> ctor, Stream<Y> rewrites) {
        List<Y> list = rewrites
            .flatMap(item -> streamFlatten(recursive, item))
            .collect(Collectors.toList());

        Y result = (list.isEmpty())
            ? null
            : (list.size() == 1)
                ? list.get(0)
                : ctor.apply(list);

        return result;
    }

    public static <X, Y extends Function<X, X>> Y flatten(boolean recursive, Function<List<Y>, Y> ctor, Stream<Y> rewrites) {
        Y result = flattenOrNull(recursive, ctor, rewrites);
        if (result == null) {
            result = ctor.apply(List.of());
        }
        return result;
    }
}
