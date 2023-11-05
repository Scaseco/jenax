package org.aksw.jenax.model.table.domain.api;

import org.aksw.jenax.annotation.reprogen.ResourceView;

@ResourceView
public interface ColumnSortCondition {
    Boolean isAscending();
    ColumnSortCondition setAscending();

    // Min / Max / Avg / etc
    String getAggregator();
    ColumnSortCondition setAggregator();

    ColumnItem getColumn();
}
