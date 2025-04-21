package org.aksw.jenax.graphql.sparql.v2.io;

import java.util.Objects;
import java.util.Stack;

import org.aksw.jenax.graphql.sparql.v2.gon.model.GonProviderApi;
import org.aksw.jenax.graphql.sparql.v2.util.ObjectUtils;

/** Writer with an in-memory destination.
 *  The produced object can be retrieved with {@link #getProduct()}. */
public class ObjectNotationWriterViaGon<T, K, V>
    implements ObjectNotationWriterInMemory<T, K, V>
{
    protected GonProviderApi<T, K, V> provider;
    protected Stack<Object> stack = new Stack<>();

    /** A name (or more general key) must be followed be immediately followed by
     *  an array, literal or object. Therefore a plain attribute is sufficient. */
    protected K pendingKey;

    /** The built object */
    protected T product;

    protected ObjectNotationWriterViaGon(GonProviderApi<T, K, V> provider) {
        super();
        this.provider = Objects.requireNonNull(provider);
    }

    public static <T, K, V> ObjectNotationWriterViaGon<T, K, V> of(GonProviderApi<T, K, V> provider) {
        return new ObjectNotationWriterViaGon<>(provider);
    }

    @Override
    public T getProduct() {
        return (T)product;
    }

    @Override
    public void clear() {
        this.product = null;
    }

    /** no-op on this class. */
    @Override
    public void flush() {
    }

    /** Append to the current element on the stack. */
    protected void put(Object elt) {
        if (pendingKey != null) {
            // Elt follows a pending key
            Object current = stack.peek();
            provider.setProperty(current, pendingKey, elt);
            pendingKey = null;
        } else if (stack.isEmpty()) {
            // Elt is the root element
            Object tmp = elt;
            product = (T)tmp;
        } else {
            // Elt is an array element
            Object current = stack.peek();
            if (provider.isArray(current)) {
                provider.addElement(current, elt);
            } else {
                throw new IllegalStateException("Illegal json structure: current top of stack: " + ObjectUtils.getClass(current) + ", attempted contribution: " + ObjectUtils.getClass(elt));
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
        Object elt = provider.newDirectLiteral(value);
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
