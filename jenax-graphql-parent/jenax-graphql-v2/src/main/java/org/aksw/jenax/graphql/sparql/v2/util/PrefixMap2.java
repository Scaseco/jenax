package org.aksw.jenax.graphql.sparql.v2.util;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.riot.system.PrefixEntry;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.shared.PrefixMapping;

public class PrefixMap2
    implements PrefixMap
{
    protected PrefixMap global;
    protected PrefixMap local;

    public PrefixMap2(PrefixMap global, PrefixMap local) {
        super();
        this.global = Objects.requireNonNull(global);
        this.local = Objects.requireNonNull(local);;
    }

    public PrefixMap getGlobal() {
        return global;
    }

    public PrefixMap getLocal() {
        return local;
    }

    @Override
    public String get(String prefix) {
        String result = local.get(prefix);
        if (result == null) {
            result = global.get(prefix);
        }
        return result;
    }

    @Override
    public Map<String, String> getMapping() {
        return MapUtils.union(global.getMapping(), local.getMapping());
    }

    @Override
    public Map<String, String> getMappingCopy() {
        Map<String, String> result = global.getMappingCopy();
        result.putAll(local.getMapping());
        return result;
    }

    @Override
    public void forEach(BiConsumer<String, String> action) {
        getMapping().forEach(action);
    }

    @Override
    public Stream<PrefixEntry> stream() {
        return getMapping().entrySet().stream().map(e -> PrefixEntry.create(e.getKey(), e.getValue()));
    }

    @Override
    public void add(String prefix, String iriString) {
        local.add(prefix, iriString);
    }

    @Override
    public void putAll(PrefixMap pmap) {
        local.putAll(pmap);
    }

    @Override
    public void putAll(PrefixMapping pmap) {
        local.putAll(pmap);
    }

    @Override
    public void putAll(Map<String, String> mapping) {
        local.putAll(mapping);
    }

    @Override
    public void delete(String prefix) {
        // TODO Store deletions in an extra set
        local.delete(prefix);
    }

    @Override
    public void clear() {
        local.clear();
    }

    @Override
    public boolean containsPrefix(String prefix) {
        return local.containsPrefix(prefix) || global.containsPrefix(prefix);
    }

    @Override
    public String abbreviate(String uriStr) {
        String result = local.abbreviate(uriStr);
        if (result == null) {
            result = global.abbreviate(uriStr);
        }
        return result;
    }

    @Override
    public Pair<String, String> abbrev(String uriStr) {
        Pair<String, String> result = local.abbrev(uriStr);
        if (result == null) {
            result = global.abbrev(uriStr);
        }
        return result;
    }

    @Override
    public String expand(String prefixedName) {
        String result = local.expand(prefixedName);
        if (result == null) {
            result = global.expand(prefixedName);
        }
        return result;
    }

    @Override
    public String expand(String prefix, String localName) {
        String result = local.expand(prefix, localName);
        if (result == null) {
            result = global.expand(prefix, localName);
        }
        return result;
    }

    @Override
    public boolean isEmpty() {
        return global.isEmpty() && local.isEmpty();
    }

    @Override
    public int size() {
        return getMapping().size();
    }
}
