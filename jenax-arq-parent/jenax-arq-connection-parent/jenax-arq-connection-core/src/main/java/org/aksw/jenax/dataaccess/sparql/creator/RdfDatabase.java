package org.aksw.jenax.dataaccess.sparql.creator;

public interface RdfDatabase {
    /**
     * Return the set of files associated with this database.
     * Null if not applicable.
     */
    RdfDatabaseFileSet getFileSet();
}
