package org.aksw.jenax.dataaccess.sparql.creator;

/**
 * Class to capture the information about an RDF database.
 */
public interface RDFDatabase {
    /**
     * Get the set of files associated with this database.
     * Null if not applicable.
     */
    FileSet getFileSet();
}
