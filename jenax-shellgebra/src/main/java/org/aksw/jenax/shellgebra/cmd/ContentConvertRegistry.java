package org.aksw.jenax.shellgebra.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.aksw.shellgebra.registry.codec.CodecRegistry;
import org.aksw.shellgebra.registry.content.Tool;
import org.aksw.shellgebra.shim.cmd.JavaStreamTransform;

public class ContentConvertRegistry {
    protected List<JavaContentConvertProvider> javaProviders = new ArrayList<>();
    protected List<ContentConvertProvider> cmdProviders = new ArrayList<>();

    private static ContentConvertRegistry defaultRegistry = null;

    public static ContentConvertRegistry get() {
        if (defaultRegistry == null) {
            synchronized (CodecRegistry.class) {
                if (defaultRegistry == null) {
                    defaultRegistry = new ContentConvertRegistry();
                    loadDefaults(defaultRegistry);
                }
            }
        }
        return defaultRegistry;
    }

    public Optional<JavaStreamTransform> getJavaConverter(String srcLang, String tgtFormat, String base) {
        return getJavaConverter(new OpSpecContentConvertRdf(srcLang, tgtFormat, base));
    }

    public Optional<JavaStreamTransform> getJavaConverter(OpSpecContentConvertRdf spec) {
        return javaProviders.stream()
            .map(provider -> provider.getConverter(spec))
            .flatMap(Optional::stream)
            .findFirst();
    }

    public List<Tool> getCmdConverter(String srcLang, String tgtFormat, String base) {
        return getCmdConverter(new OpSpecContentConvertRdf(srcLang, tgtFormat, base));
    }

    public List<Tool> getCmdConverter(OpSpecContentConvertRdf spec) {
        return cmdProviders.stream()
            .map(provider -> provider.getConverter(spec))
            .flatMap(Optional::stream)
            .toList();
            // .findFirst();
    }

    public static void loadDefaults(ContentConvertRegistry registry) {
        registry.addJavaContentConvertProvider(new JavaContentConvertProviderOverJena());
        registry.addCmdContentConvertProvider(new ContentConvertProviderOverRapper());
    }

    public void addJavaContentConvertProvider(JavaContentConvertProvider provider) {
        Objects.requireNonNull(provider);
        javaProviders.add(0, provider);
    }

    public void addCmdContentConvertProvider(ContentConvertProvider provider) {
        Objects.requireNonNull(provider);
        cmdProviders.add(0, provider);
    }
}
