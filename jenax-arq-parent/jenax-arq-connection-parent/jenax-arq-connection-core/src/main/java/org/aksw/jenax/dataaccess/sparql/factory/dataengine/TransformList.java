package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.util.List;
import java.util.function.Function;

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
}
