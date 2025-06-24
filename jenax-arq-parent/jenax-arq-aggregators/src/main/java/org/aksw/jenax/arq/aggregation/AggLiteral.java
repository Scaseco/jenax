package org.aksw.jenax.arq.aggregation;

import java.util.Set;

import org.aksw.jenax.arq.util.binding.BindingMapper;
import org.aksw.jenax.arq.util.binding.BindingMapperVarAware;
import org.apache.jena.sparql.core.Var;

public class AggLiteral<T>
    implements Agg<T>
{
    private BindingMapper<T> mapper;

    public AggLiteral(BindingMapper<T> mapper) {
        this.mapper = mapper;
    }

    @Override
    public Acc<T> createAccumulator() {
        Acc<T> result = new AccLiteral<T>(mapper);
        return result;
    }

    @Override
    public Set<Var> getDeclaredVars() {
        Set<Var> result = mapper instanceof BindingMapperVarAware<?>
            ? ((BindingMapperVarAware<?>)mapper).getVarsMentioned()
            : null // Collections.emptySet()
            ;

        return result;
    }

    public static <T> AggLiteral<T> create(BindingMapper<T> mapper) {
        AggLiteral<T> result = new AggLiteral<T>(mapper);
        return result;
    }
}