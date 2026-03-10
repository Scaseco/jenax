package org.aksw.jenax.arq.util.tuple.adapter;

import java.util.stream.Stream;

import org.aksw.commons.tuple.finder.TupleFinder3;
import org.apache.jena.rdfs.engine.MapperX;
import org.apache.jena.rdfs.engine.Match;

public class MatchOverTupleFinder3<D, C>
    implements Match<C, D>
{
    protected TupleFinder3<D, C> delegate;
    protected MapperX<C, D> mapperX;

    protected MatchOverTupleFinder3(TupleFinder3<D, C> delegate, MapperX<C, D> mapperX) {
        super();
        this.delegate = delegate;
        this.mapperX = mapperX;
    }

    public static <T, X> Match<X, T> wrap(TupleFinder3<T, X> delegate, MapperX<X, T> mapperX) {
        return new MatchOverTupleFinder3<>(delegate, mapperX);
    }

    @Override
    public Stream<D> match(C s, C p, C o) {
        return delegate.find(s, p, o);
    }

    @Override
    public MapperX<C, D> getMapper() {
        return mapperX;
    }
}
