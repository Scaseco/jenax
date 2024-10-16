package org.aksw.jenax.io.json.writer;

import java.util.Objects;
import java.util.Stack;

import org.aksw.jenax.io.json.gon.GonProvider;

/** Writer with an in-memory destination.
 *  The produced object can be retrieved with {@link #getProduct()}. */
public class ObjectNotationWriterViaGon<K, V>
    implements ObjectNotationWriter<K, V>
{
    protected GonProvider<K, V> provider;
    protected Stack<Object> stack = new Stack<>();

    /** A name (or more general key) must be followed be immediately followed by
     *  an array, literal or object. Therefore a plain attribute is sufficient. */
    protected K pendingKey;

    /** The built object */
    protected Object product;

    protected ObjectNotationWriterViaGon(GonProvider<K, V> provider) {
        super();
        this.provider = Objects.requireNonNull(provider);
    }

    public static <K, V> ObjectNotationWriterViaGon<K, V> of(GonProvider<K, V> provider) {
        return new ObjectNotationWriterViaGon<>(provider);
    }

    public Object getProduct() {
        return product;
    }

    @Override
    public void flush() {
    }

    /** Append to the current element on the stack. */
    protected void put(Object elt) {
        if (pendingKey != null) {
            Object current = stack.peek();
            provider.setProperty(current, pendingKey, elt);
            pendingKey = null;
        } else if (stack.isEmpty()) {
            product = elt;
        } else {
            Object current = stack.peek();
            if (provider.isArray(current)) {
                provider.addElement(current, elt);
            } else {
                throw new IllegalStateException();
            }
        }
    }

    @Override
    public ObjectNotationWriter<K, V> beginArray() {
        Object elt = provider.newArray();
        put(elt);
        stack.add(elt);
        return this;
    }

    @Override
    public ObjectNotationWriter<K, V> endArray() {
        Object elt = stack.peek();
        if (!provider.isArray(elt)) {
            throw new IllegalStateException();
           }
        stack.pop();
        return this;
    }

    @Override
    public ObjectNotationWriter<K, V> beginObject() {
        Object elt = provider.newObject();
        put(elt);
        stack.add(elt);
        return this;
    }

    @Override
    public ObjectNotationWriter<K, V> endObject() {
        Object elt = stack.peek();
        if (!provider.isObject(elt)) {
            throw new IllegalStateException();
           }
        stack.pop();
        return this;
    }

    @Override
    public ObjectNotationWriter<K, V> name(K key) {
        Object elt = stack.peek();
        if (!provider.isObject(elt)) {
            throw new IllegalStateException("Element under construction is not an object.");
        }
        if (pendingKey != null) {
            throw new IllegalStateException("A pending name has already been set.");
        }
        pendingKey = key;
        return this;
    }

    @Override
    public ObjectNotationWriter<K, V> value(V value) {
        Object elt = provider.newLiteral(value);
        put(elt);
        return this;
    }

    @Override
    public ObjectNotationWriter<K, V> nullValue() {
        Object nil = provider.newNull();
        put(nil);
        return this;
    }
}
