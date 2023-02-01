package org.aksw.jenax.arq.util.tuple.adapter;

import java.util.stream.Stream;

import org.aksw.commons.tuple.finder.TupleFinder3;
import org.apache.jena.rdfs.engine.Match;

public class MatchOverTupleFinder3<D, C>
    implements Match<C, D>
{
    protected TupleFinder3<D, C> delegate;

    protected MatchOverTupleFinder3(TupleFinder3<D, C> delegate) {
        super();
        this.delegate = delegate;
    }

    public static <T, X> Match<X, T> wrap(TupleFinder3<T, X> delegate) {
        return new MatchOverTupleFinder3<>(delegate);
    }

    @Override
    public Stream<D> match(C s, C p, C o) {
        return delegate.find(s, p, o);
    }
}
