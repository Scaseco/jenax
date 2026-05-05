package org.aksw.jena_sparql_api.stmt;

import org.aksw.jenax.stmt.util.SparqlStmtIterator;
import org.aksw.jenax.stmt.util.SparqlStmtUtils;
import org.apache.jena.query.QueryParseException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.junit.jupiter.api.Test;

public class TestValidateSparqlQueryList {

    /**
     * Expect a parse error in an erroneous file
     *
     * @throws Exception
     */
   @Test
    public void testSparqlFileForParseError() {
        PrefixMapping pm = new PrefixMappingImpl();
        pm.setNsPrefixes(PrefixMapping.Extended);

        // TODO Validate the line/column numbers of the syntax error

        assertThrows(QueryParseException.class, () -> {
            SparqlStmtIterator it = SparqlStmtUtils.processFile(pm, "syntax-error.sparql");
            while(it.hasNext()) {
                it.next();
                // SparqlStmt stmt = it.next();
                // System.out.println(stmt.getAsQueryStmt().getQuery());
            }
        });
    }
}
