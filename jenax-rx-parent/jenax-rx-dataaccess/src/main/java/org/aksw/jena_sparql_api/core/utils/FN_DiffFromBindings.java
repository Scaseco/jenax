package org.aksw.jena_sparql_api.core.utils;

import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.binding.Binding;

import com.google.common.base.Function;

public class FN_DiffFromBindings
    implements Function<Iterable<? extends Binding>, Diff<Set<Quad>>>
{
    private Diff<? extends Iterable<Quad>> quadDiff;

    public FN_DiffFromBindings(Diff<? extends Iterable<Quad>> quadDiff) {
        this.quadDiff = quadDiff;
    }

    @Override
    public Diff<Set<Quad>> apply(Iterable<? extends Binding> bindings) {
        Diff<Set<Quad>> result = UpdateDiffUtils.buildDiff(bindings, quadDiff);
        return result;
    }


    public static FN_DiffFromBindings create(Diff<? extends Iterable<Quad>> quadDiff) {
        FN_DiffFromBindings result = new FN_DiffFromBindings(quadDiff);
        return result;
    }
}
