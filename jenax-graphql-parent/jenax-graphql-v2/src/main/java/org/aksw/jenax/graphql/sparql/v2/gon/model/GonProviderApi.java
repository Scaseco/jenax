package org.aksw.jenax.graphql.sparql.v2.gon.model;

public interface GonProviderApi<T, K, V> // V extends T>
        extends GonProvider<K, V>
{
    T upcast(Object element);

    @Override
    T parse(String str);

    @Override
    T newArray();

    @Override
    T newObject();

    @Override
    T newNull();

    public static class GonProviderApiWrapper<K, V>
        implements GonProviderWrapper<K, V>, GonProviderApi<Object, K, V> {

        protected GonProvider<K, V> delegate;

        public GonProviderApiWrapper(GonProvider<K, V> delegate) {
            super();
            this.delegate = delegate;
        }

        @Override
        public GonProvider<K, V> getDelegate() {
            return delegate;
        }

        @Override
        public Object parse(String str) {
            return GonProviderWrapper.super.parse(str);
        }

        @Override
        public Object upcast(Object element) {
            return element;
        }

        @Override
        public Object newArray() {
            return GonProviderWrapper.super.newArray();
        }

        @Override
        public Object newObject() {
            return GonProviderWrapper.super.newObject();
        }

        @Override
        public Object newNull() {
            return GonProviderWrapper.super.newNull();
        }
    }

    /** Generic adapter to treat any GonProvider as a GonProviderApi of Object. */
    static <K, V> GonProviderApi<Object, K, V> wrap(GonProvider<K, V> delegate) {
        @SuppressWarnings("unchecked")
        GonProviderApi<Object, K, V> result = delegate instanceof GonProviderApi api
                ? (GonProviderApi<Object, K, V>)api
                : new GonProviderApiWrapper<>(delegate);
        return result;
    }
}
