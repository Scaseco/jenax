package org.aksw.jenax.arq.datatype;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.util.ExprUtils;

/**
 * A datatype for storing SPARQL expressions in RDF literals.
 *
 * @author Claus Stadler
 *
 */
public class RDFDatatypeExpr
    extends BaseDatatype
{
    public static final String IRI = "http://jsa.aksw.org/dt/sparql/expr";
    public static final RDFDatatypeExpr INSTANCE = new RDFDatatypeExpr();

    public static RDFDatatype get() {
        return INSTANCE;
    }

    public RDFDatatypeExpr() {
        this(IRI);
    }

    public RDFDatatypeExpr(String uri) {
        super(uri);
    }

    @Override
    public Class<?> getJavaClass() {
        return Expr.class;
    }

    /**
     * Convert a value of this datatype out
     * to lexical form.
     */
    @Override
    public String unparse(Object value) {
        String result = value instanceof Expr
                ? ExprUtils.fmtSPARQL((Expr)value)
                : null;

        return result;
    }

    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    @Override
    public Expr parse(String lexicalForm) throws DatatypeFormatException {
        Expr result;
        try {
            result = ExprUtils.parse(lexicalForm);
        } catch(Exception e) {
            // TODO This is not the best place for an expr eval exception; it should go to E_StrDatatype
            throw new ExprEvalException(e);
        }
        return result;
    }
}