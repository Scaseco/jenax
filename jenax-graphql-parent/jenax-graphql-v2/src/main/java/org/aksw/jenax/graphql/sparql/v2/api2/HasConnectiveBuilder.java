package org.aksw.jenax.graphql.sparql.v2.api2;

import java.util.Objects;

public interface HasConnectiveBuilder<T extends HasConnectiveBuilder<T>>
    extends HasSelf<T>
{
    T connective(Connective connective);

    default ConnectiveSubBuilder<T> newConnectiveBuilder() {
        T self = self();
        return new ConnectiveSubBuilder<>(self);
    }

    public static class ConnectiveSubBuilder<T extends HasConnectiveBuilder<T>>
        extends ConnectiveBuilder<ConnectiveSubBuilder<T>> {

        protected T parent;

        public ConnectiveSubBuilder(T parent) {
            super();
            this.parent = Objects.requireNonNull(parent);
        }

        @Override
        public Connective build() {
            throw new IllegalStateException("Use .set() instead of .build() on sub builders.");
        }

        public T set() {
            Connective tmp = super.build();
            parent.connective(tmp);
            return parent;
        }
    }
}
