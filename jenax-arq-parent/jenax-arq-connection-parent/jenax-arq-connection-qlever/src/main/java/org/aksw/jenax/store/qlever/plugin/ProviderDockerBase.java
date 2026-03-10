package org.aksw.jenax.store.qlever.plugin;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.factory.dataengine.Provider;

public abstract class ProviderDockerBase<T>
    implements Provider<T>
{
    protected String prefix;

    public static record ImageAndTag(String imageName, String tag) {}

    public ProviderDockerBase(String prefix) {
        super();
        this.prefix = Objects.requireNonNull(prefix);
    }

    public static void main(String[] args) {

    }

    @Override
    public T create(String name) {
        T result = null;

        boolean isAccepted = false;
        String image = null;
        String tag = null;

        if (name.startsWith(prefix)) {
            String suffix = name.substring(prefix.length());
            if (suffix.isEmpty()) {
                isAccepted = true;
                // nothing to do.
            } else if (suffix.startsWith(":")) {
                suffix = suffix.substring(1);
                String[] parts = suffix.split(":", 2);
                if (parts.length == 1) {
                    tag = parts[0];
                } else {
                    image = parts[0];
                    tag = parts[1];
                }
                // XXX Could warn on invalid chars in image or tag name.
                isAccepted = true;
            } else {
                // rejected.
            }
        }

        if (isAccepted) {
            result = provide(image, tag);
        }

        return result;
    }

    protected abstract T provide(String imageName, String tag);
}
