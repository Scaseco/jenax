package org.aksw.jena_sparql_api.update;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.core.utils.UpdateDiffUtils;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.binding.Binding;

import com.google.common.collect.AbstractIterator;

@Deprecated
public class QuadDiffIterator
    extends AbstractIterator<Diff<Set<Quad>>>
{
    private Iterator<? extends Iterable<? extends Binding>> itBindings;
    private Diff<List<Quad>> quadDiff;

    public QuadDiffIterator(Iterator<? extends Iterable<? extends Binding>> itBindings, Diff<List<Quad>> quadDiff) {
        this.itBindings = itBindings;
        this.quadDiff = quadDiff;
    }

    @Override
    protected Diff<Set<Quad>> computeNext() {
        Diff<Set<Quad>> result;

        if(itBindings.hasNext()) {
            Iterable<? extends Binding> bindings = itBindings.next();
            result = UpdateDiffUtils.buildDiff(bindings, quadDiff);
        } else {
            result = endOfData();
        }

        return result;
    }
}
