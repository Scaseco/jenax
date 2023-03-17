package org.aksw.jena_sparql_api.sparql.ext.json;

import java.util.function.Supplier;

import org.aksw.commons.util.memoize.MemoizedSupplierImpl;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.ToNumberPolicy;

public class RDFDatatypeJson
    extends BaseDatatype
{
    private static final Logger logger = LoggerFactory.getLogger(RDFDatatypeJson.class);

    public static final String LEGACY_IRI = XSD.getURI() + "json";
    private static final RDFDatatypeJson INSTANCE = new RDFDatatypeJson();

    public static RDFDatatypeJson get() {
        return INSTANCE;
    }

    /** Gson is initialized lazily because it causes a noticeable startup delay */
    private Supplier<Gson> gsonSupplier;

    public RDFDatatypeJson() {
        this(SparqlX_Json_Terms.Datatype);
    }

    public RDFDatatypeJson(String uri) {
        this(uri, () -> createGson());
    }
    
    // Workaround for spark's old guava version which may not support setLenient
    public static Gson createGson() {
        GsonBuilder builder = new GsonBuilder();
        try {
            builder.setLenient();
        } catch(NoSuchMethodError e) {
            logger.warn("Gson.setLenient not available");
        }

        try {
            builder.setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE);
            builder.setNumberToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE);
        } catch(NoSuchMethodError e) {
        	// May fail in e.g. hadoop environments
            logger.warn("Gson.setObjectToNumberStrategy and/or Gson.setNumberToNumberStrategy not available");
        }

        Gson result = builder.create();
        return result;
    }

    @Override
    public boolean isValidValue(Object valueForm) {
        boolean result = valueForm instanceof JsonElement;
        return result;
    }

    public RDFDatatypeJson(String uri, Supplier<Gson> gsonSupplier) {
        super(uri);
        this.gsonSupplier = MemoizedSupplierImpl.of(gsonSupplier);
    }

    public Gson getGson() {
        return gsonSupplier.get();
    }

    @Override
    public Class<?> getJavaClass() {
        return JsonElement.class;
    }

    /**
     * Convert a value of this datatype out
     * to lexical form.
     */
    @Override
    public String unparse(Object value) {
        String result = JenaJsonUtils.convertJsonOrValueToString(value, getGson());
        return result;
    }

    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    @Override
    public JsonElement parse(String lexicalForm) throws DatatypeFormatException {
        JsonElement result;
        try {
            result = getGson().fromJson(lexicalForm, JsonElement.class);
        } catch(Exception e) {
            throw new DatatypeFormatException(lexicalForm, this, e);
        }
        return result;
    }
}
