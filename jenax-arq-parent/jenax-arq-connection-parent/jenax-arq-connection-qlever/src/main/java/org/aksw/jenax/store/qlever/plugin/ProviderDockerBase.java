package org.aksw.jenax.store.qlever.plugin;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.factory.dataengine.Provider;

public abstract class ProviderDockerBase<T>
    implements Provider<T>
{
    protected String prefix;

    public ProviderDockerBase(String prefix) {
        super();
        this.prefix = Objects.requireNonNull(prefix);
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
                String[] tagAndImage = suffix.split(":", 2);
                tag = tagAndImage[0];
                image = tagAndImage.length >= 2 ? tagAndImage[1] : null;
                // TODO WARN if more than 2 components.
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
