package org.aksw.jenax.stmt.core;

import org.aksw.jenax.arq.util.update.UpdateRequestUtils;
import org.aksw.jenax.stmt.parser.update.SparqlUpdateParserImpl;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.Syntax;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;

public class SparqlStmtUpdate
    extends SparqlStmtBase
{
    private static final long serialVersionUID = 1L;

    protected transient UpdateRequest updateRequest;

    public SparqlStmtUpdate(Update update) {
        this(new UpdateRequest(update));
    }

    public SparqlStmtUpdate(UpdateRequest updateRequest) {
        this(updateRequest, updateRequest.toString());
    }

    public SparqlStmtUpdate(String updateRequestStr) {
        this(null, updateRequestStr);
    }

    public SparqlStmtUpdate(UpdateRequest updateRequest, String updateRequestStr) {
        this(updateRequest, updateRequestStr, null, null);
    }

    public SparqlStmtUpdate(String updateRequestStr, QueryParseException parseException) {
        this(null, updateRequestStr, null, parseException);
    }

    public SparqlStmtUpdate(UpdateRequest updateRequest, String updateRequestStr, String parserBase, QueryParseException parseException) {
        super(updateRequestStr, parserBase, parseException);
        this.updateRequest = updateRequest;
    }

    private Object readResolve() {
        UpdateRequest update = null;
        QueryParseException parseException = null;
        try {
            // update = SparqlUpdateParserImpl.createAsGiven().apply(originalString);
            update = SparqlUpdateParserImpl.create(SparqlParserConfig.newInstance()
                    .setSyntax(Syntax.syntaxARQ)
                    .setPrologue(new Prologue())
                    .setBaseURI(parserBase))
                    .apply(originalString);
        } catch (QueryParseException e) {
            parseException = e;
        }
        return new SparqlStmtUpdate(update, originalString, parserBase, parseException);
    }

    @Override
    public SparqlStmtUpdate clone() {
        UpdateRequest clone = updateRequest != null
                ? UpdateRequestUtils.clone(updateRequest)
                : null;

        return new SparqlStmtUpdate(clone, originalString, parserBase, parseException);
    }


    public UpdateRequest getUpdateRequest() {
        return updateRequest;
    }

    @Override
    public boolean isParsed() {
        return updateRequest != null;
    }

    @Override
    public boolean isUpdateRequest() {
        return true;
    }

    @Override
    public SparqlStmtUpdate getAsUpdateStmt() {
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((updateRequest == null) ? 0 : updateRequest.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SparqlStmtUpdate other = (SparqlStmtUpdate) obj;
        if (updateRequest == null) {
            if (other.updateRequest != null)
                return false;
        } else if (!updateRequest.equals(other.updateRequest))
            return false;
        return true;
    }

    @Override
    public String toString() {
        String result = updateRequest != null
                ? updateRequest.toString()
                : super.toString();

        return result;
    }

    @Override
    public PrefixMapping getPrefixMapping() {
        PrefixMapping result = updateRequest != null
                ? updateRequest.getPrefixMapping()
                : null;
        return result;
    }

}
