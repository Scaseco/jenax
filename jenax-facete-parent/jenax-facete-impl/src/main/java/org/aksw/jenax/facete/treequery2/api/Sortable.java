package org.aksw.jenax.facete.treequery2.api;

import org.apache.jena.query.Query;

public interface Sortable<T> {
    /**
     * Updates or adds the first sort condition of this query node's variable in the list of sort conditions
     */
    T sort(int sortDirection);

    /** Returns the direction of the first sort condition that matches this query node's variable */
    int getSortDirection();

    default T sortAsc() {
        return sort(Query.ORDER_ASCENDING);
    }

    default T sortNone() {
        return sort(Query.ORDER_UNKNOW);
    }

    default T sortDefault() {
        return sort(Query.ORDER_DEFAULT);
    }

    default T sortDesc() {
        return sort(Query.ORDER_DESCENDING);
    }
}
