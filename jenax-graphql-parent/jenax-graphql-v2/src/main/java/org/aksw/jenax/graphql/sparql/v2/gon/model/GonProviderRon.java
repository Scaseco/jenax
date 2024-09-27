package org.aksw.jenax.graphql.sparql.v2.gon.model;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;

import org.aksw.jenax.ron.RdfArray;
import org.aksw.jenax.ron.RdfArrayImpl;
import org.aksw.jenax.ron.RdfElement;
import org.aksw.jenax.ron.RdfLiteral;
import org.aksw.jenax.ron.RdfLiteralImpl;
import org.aksw.jenax.ron.RdfNull;
import org.aksw.jenax.ron.RdfObject;
import org.aksw.jenax.ron.RdfObjectImpl;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.path.P_Path0;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GonProviderRon
    implements GonProviderApi<RdfElement, P_Path0, RdfLiteral>
{
    private static GonProviderRon INSTANCE;

    public static GonProviderRon getInstance() {
        if (INSTANCE == null) {
            synchronized (GonProviderRon.class) {
                if (INSTANCE == null) {
                    INSTANCE = new GonProviderRon();
                }
            }
        }
        return INSTANCE;
    }

    protected GonProviderRon() {
        super();
    }

    public static GonProviderGson of() {
        return of(new GsonBuilder().setPrettyPrinting().setLenient().create());
    }

    public static GonProviderGson of(Gson gson) {
        Objects.requireNonNull(gson);
        return new GonProviderGson(gson);
    }

    @Override
    public RdfLiteral upcast(Object element) {
        return (RdfLiteral)element;
    }

    @Override
    public RdfLiteral parse(String str) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RdfObject newObject() {
        return new RdfObjectImpl();
    }

    @Override
    public boolean isObject(Object obj) {
        return obj instanceof RdfObject;
    }

    protected static RdfElement asElement(Object obj) {
        RdfElement result = (RdfElement)obj;
        return result;
    }

    protected static RdfObject asObject(Object obj) {
        RdfObject result = asElement(obj).getAsObject();
        return result;
    }

    protected static RdfArray asArray(Object obj) {
        RdfArray result = asElement(obj).getAsArray();
        return result;
    }

    protected static RdfLiteral asPrimitive(Object obj) {
        RdfLiteral result = asElement(obj).getAsLiteral();
        return result;
    }

    @Override
    public void setProperty(Object obj, P_Path0 key, Object value) {
        asObject(obj).add(key, asElement(value));
    }

    @Override
    public Object getProperty(Object obj, Object key) {
        return asObject(obj).get((String)key);
    }

    @Override
    public void removeProperty(Object obj, Object key) {
        asObject(obj).remove((P_Path0)key);
    }

    @Override
    public Iterator<Entry<P_Path0, Object>> listProperties(Object obj) {
        return (Iterator)asObject(obj).getMembers().entrySet().iterator();
    }

    @Override
    public RdfArray newArray() {
        return new RdfArrayImpl();
    }

    @Override
    public boolean isArray(Object obj) {
        return obj instanceof RdfArray;
    }

    @Override
    public void addElement(Object arr, Object value) {
        asArray(arr).add(asElement(value));
    }

    @Override
    public void setElement(Object arr, int index, Object value) {
        asArray(arr).set(index, asElement(value));
    }

    @Override
    public void removeElement(Object arr, int index) {
        asArray(arr).remove(index);
    }

    @Override
    public Iterator<Object> listElements(Object arr) {
        return (Iterator)asArray(arr).iterator();
    }

    @Override
    public Object newDirectLiteral(RdfLiteral value) {
        return value;
    }

    @Override
    public RdfLiteral newLiteral(boolean value) {
        return new RdfLiteralImpl(NodeValue.makeBoolean(value).asNode());
    }

    @Override
    public RdfLiteral newLiteral(Number value) {
        return new RdfLiteralImpl(NodeFactory.createLiteralByValue(value));
    }

    @Override
    public RdfLiteral newLiteral(String value) {
        return new RdfLiteralImpl(NodeFactory.createLiteralString(value));
    }

    @Override
    public boolean isLiteral(Object obj) {
        return obj instanceof RdfLiteral;
    }

    @Override
    public RdfLiteral getLiteral(Object obj) {
        return asPrimitive(obj);
    }

    @Override
    public RdfNull newNull() {
        return new RdfNull();
    }

    @Override
    public boolean isNull(Object obj) {
        return asElement(obj).isNull();
    }
}
