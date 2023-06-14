package org.aksw.jenax.model.table.domain.api;

import java.util.List;

public interface HasOrder {
    List<ColumnSortCondition> getSortConditions();
}
