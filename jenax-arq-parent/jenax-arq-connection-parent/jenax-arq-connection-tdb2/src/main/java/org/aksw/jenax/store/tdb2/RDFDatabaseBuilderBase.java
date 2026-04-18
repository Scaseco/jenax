package org.aksw.jenax.store.tdb2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfo;
import org.aksw.jenax.dataaccess.sparql.creator.RDFDatabaseBuilder;
import org.aksw.jenax.sparql.query.rx.RDFDataMgrEx;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

public abstract class RDFDatabaseBuilderBase<X extends RDFDatabaseBuilderBase<X>>
    implements RDFDatabaseBuilder<X>
{
    // XXX Could add check for whether a file is repeatedly loaded into the same graph.
    // private Map<Path, Node> fileToGraph = new LinkedHashMap<>();
    public record FileArg(Path path, Lang lang, List<String> encodings, Node graph, Boolean splittable) {}

    protected List<FileArg> args = new ArrayList<>();

    protected Map<String, Object> properties = new LinkedHashMap<>();
    protected String name;
    protected Path outputFolder;

    protected abstract Collection<Lang> getSupportedLangs();

    @Override
    public X addPath(String source, Lang lang, Node graph, Boolean splittable) throws IOException {
        Collection<Lang> supportedLangs = getSupportedLangs();
        Path path = Path.of(source);
        RdfEntityInfo entityInfo = RDFDataMgrEx.probeEntityInfo(() -> Files.newInputStream(path, StandardOpenOption.READ), supportedLangs);
        if (lang == null) {
            String contentType = entityInfo.getContentType();
            lang = RDFLanguages.contentTypeToLang(contentType);
        }
        addPath(path, graph, entityInfo.getContentEncodings(), lang, splittable);
        return self();
    }

    protected void addPath(Path source, Node graph, List<String> encodings, Lang lang, Boolean splittable) {
        FileArg arg = new FileArg(source, lang, encodings, graph, splittable);
        args.add(arg);
    }

    @Override
    public X setProperty(String key, Object value) {
        properties.put(key, value);
        return self();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(String key) {
        Object value = properties.get(key);
        return (T)value;
    }

    @Override
    public X setOutputFolder(Path outputFolder) {
        this.outputFolder = outputFolder;
        return self();
    }

    @Override
    public X setName(String name) {
        this.name = name;
        return self();
    }

    public static String getGraphLabel(Node graphNode) {
        String result = graphNode == null ? "(default graph)" : graphNode.toString();
        return result;
    }
}
