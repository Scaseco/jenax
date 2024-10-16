package org.aksw.jenax.graphql.sparql;

import java.util.IdentityHashMap;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;

interface Cxt<T, F> {
    T getObject();
    F getField();
}

public abstract class ContextTree<T, F, V> {
    protected V root;
    protected Table<T, F, V> nodeToContext = Tables.newCustomTable(new IdentityHashMap<>(), IdentityHashMap::new);

    abstract V createContext(T parent, F field);
}


class NamespaceTree<T, F>
    extends ContextTree<T, F, Context> {

    @Override
    Context createContext(T parent, F field) {
        return null;
    }

}
