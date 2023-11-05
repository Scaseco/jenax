package org.aksw.jenax.model.table.domain.api;

public interface ColumnItem
    extends TableDef
//    extends HasColumnSet
{
    /** Get the column set to which this item belongs */
    HasColumnSet getOwner();

    // String getLocalId();       // If unspecified then an Id will be inferred from the resource's (internal) id
    // String getSparqlVarName(); // If specified then generated sparql query will use this var name even if it clashes

    String getProperty();
    ColumnItem setProperty(String property);

    Boolean isForward();
    ColumnItem setForward(Boolean isForward);
}
