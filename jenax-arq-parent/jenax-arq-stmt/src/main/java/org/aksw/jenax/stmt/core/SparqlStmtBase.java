package org.aksw.jenax.stmt.core;

import org.apache.jena.query.QueryParseException;

public abstract class SparqlStmtBase
    implements SparqlStmt
{
    private static final long serialVersionUID = 1L;

    // The base URL used for parsing the originalString
    protected String parserBase;
    protected String originalString;
    protected QueryParseException parseException;

    /* For (de)serialization */
    SparqlStmtBase() {
    }

    public SparqlStmtBase(String originalString) {
        this(originalString, null);
    }

    public SparqlStmtBase(String originalString, String parserBase) {
        this(originalString, parserBase, null);
    }

    public SparqlStmtBase(String originalString, String parserBase, QueryParseException parseException) {
        super();
        this.originalString = originalString;
        this.parserBase = parserBase;
        this.parseException = parseException;
    }

    @Override
    public abstract SparqlStmt clone();

    @Override
    public String getOriginalString() {
        return originalString;
    }

    @Override
    public QueryParseException getParseException() {
        return parseException;
    }

    @Override
    public boolean isUnknown() {
        return false;
    }

    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public boolean isUpdateRequest() {
        return false;
    }

    @Override
    public SparqlStmtUpdate getAsUpdateStmt() {
        throw new RuntimeException("Invalid type");    }

    @Override
    public SparqlStmtQuery getAsQueryStmt() {
        throw new RuntimeException("Invalid type");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((originalString == null) ? 0 : originalString.hashCode());
        result = prime * result
                + ((parseException == null) ? 0 : parseException.hashCode());
        result = prime * result
                + ((parserBase == null) ? 0 : parserBase.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SparqlStmtBase other = (SparqlStmtBase) obj;
        if (originalString == null) {
            if (other.originalString != null)
                return false;
        } else if (!originalString.equals(other.originalString))
            return false;
        if (parseException == null) {
            if (other.parseException != null)
                return false;
        } else if (!parseException.equals(other.parseException))
            return false;
        if (parserBase == null) {
            if (other.parserBase != null)
                return false;
        } else if (!parserBase.equals(other.parserBase))
            return false;
        return true;
    }

    @Override
    public String toString() {
        String result = parseException == null
                ? originalString
                : "SparqlStmtBase [originalString=" + originalString
                + ", parserBase=" + parserBase
                + ", parseException=" + parseException + "]";

        return result;
    }
}
