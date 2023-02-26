package org.aksw.jena_sparql_api.sparql.ext.json;

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

    public static final String IRI = XSD.getURI() + "json";
    private static final RDFDatatypeJson INSTANCE = new RDFDatatypeJson();

    public static RDFDatatypeJson get() {
        return INSTANCE;
    }

    private Gson gson;

    public RDFDatatypeJson() {
        this(IRI);
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
            logger.warn("Gson.setObjectToNumberStrategy and/or Gson.setNumberToNumberStrategy not available");
        }


        Gson result = builder.create();
        return result;
    }

    public RDFDatatypeJson(String uri) {
        this(uri, createGson());
    }

    public RDFDatatypeJson(String uri, Gson gson) {
        super(uri);
        this.gson = gson;
    }

    public Gson getGson() {
        return gson;
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
        String result = gson.toJson(value);
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
            result = gson.fromJson(lexicalForm, JsonElement.class);
        } catch(Exception e) {
            throw new DatatypeFormatException(lexicalForm, this, e);
        }
        return result;
    }
}
