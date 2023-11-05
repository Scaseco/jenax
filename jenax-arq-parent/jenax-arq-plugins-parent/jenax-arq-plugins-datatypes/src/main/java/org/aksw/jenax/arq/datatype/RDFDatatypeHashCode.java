package org.aksw.jenax.arq.datatype;

import org.aksw.jenax.norse.NorseTerms;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;

import com.google.common.hash.HashCode;

/**
 * Datatype for a (guava) hash code.
 * Primariy motivation is to compute dataset hashes.
 */
public class RDFDatatypeHashCode
    extends BaseDatatype
{
    public static final String IRI = NorseTerms.NS + "hashCode"; // RDF.uri + "array";
    public static final RDFDatatypeHashCode INSTANCE = new RDFDatatypeHashCode();

    public static RDFDatatype get() {
        return INSTANCE;
    }

    public RDFDatatypeHashCode() {
        this(IRI);
    }

    public RDFDatatypeHashCode(String uri) {
        super(uri);
    }

    @Override
    public Class<?> getJavaClass() {
        return HashCode.class;
    }

    /** Unparse a node list as a string */
    @Override
    public String unparse(Object hashCode) {
        String result;
        if (hashCode instanceof HashCode) {
            HashCode hc = (HashCode)hashCode;
            // Guava doc states that the result of toString can be fed to fromString
            result = hc.toString();
        } else {
            throw new DatatypeFormatException("Not a HashCode instance");
        }
        return result;
    }

    /** Parse a string as an arbitrary function and extract the arguments as an ExprList */
    @Override
    public HashCode parse(String str) {
        HashCode result = HashCode.fromString(str);
        return result;
    }
}
