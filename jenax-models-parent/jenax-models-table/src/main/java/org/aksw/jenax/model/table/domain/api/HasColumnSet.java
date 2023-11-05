package org.aksw.jenax.model.table.domain.api;

import java.util.List;
import java.util.Set;

public interface HasColumnSet
{
    /** References to the properties which to show in which order. */
    List<ColumnItem> getStaticColumns();

    /** Get all defined sub columns. There may be more definitions than that are visible. */
    Set<ColumnItem> getSubColumnDefinitions();
}
