package org.aksw.jena_sparql_api_sparql_path2.playground;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.sparql_path2.Nfa;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;

public class QefNfaSuccessor<S, T, V>
//	implements Function<Pa>
{
//    public static <S, T, E> Multimap<Pair<S, V>, Pair<V, E>> createLookupServiceSuccessor(QueryExecutionFactory qef, Nfa<S, T> nfa) {
//
//    }

    protected Nfa<S, T> nfa;
    protected QueryExecutionFactory qef;

    public Map<V, S> apply(Set<? extends Entry<S, V>> stateToVertex) {
        return null;
    }

}
