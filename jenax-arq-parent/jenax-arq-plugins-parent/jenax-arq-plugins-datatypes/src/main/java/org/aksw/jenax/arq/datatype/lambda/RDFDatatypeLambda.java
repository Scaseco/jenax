package org.aksw.jenax.arq.datatype.lambda;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.util.ExprUtils;

///**
// * A datatype for storing SPARQL expressions in RDF literals.
// *
// * @author Claus Stadler
// *
// */
public class RDFDatatypeLambda
    extends BaseDatatype
{
    public static final String IRI = "http://jsa.aksw.org/dt/sparql/lambda";
    public static final RDFDatatypeLambda INSTANCE = new RDFDatatypeLambda();

    public static RDFDatatype get() {
        return INSTANCE;
    }

    public RDFDatatypeLambda() {
        this(IRI);
    }

    public RDFDatatypeLambda(String uri) {
        super(uri);
    }

    @Override
    public Class<?> getJavaClass() {
        return Lambda.class;
    }

    /**
     * Convert a value of this datatype out
     * to lexical form.
     */
    @Override
    public String unparse(Object value) {
        String result = value instanceof Lambda
                ? Lambdas.unparse((Lambda)value)
                : null;

        return result;
    }

    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    @Override
    public Lambda parse(String lexicalForm) throws DatatypeFormatException {
        Lambda result = Lambdas.parse(lexicalForm);
        return result;
    }
}