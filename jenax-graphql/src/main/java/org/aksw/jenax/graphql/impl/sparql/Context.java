package org.aksw.jenax.graphql.impl.sparql;

import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

import org.aksw.jenax.arq.util.prefix.PrefixMap2;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.graph.PrefixMappingAdapter;

import graphql.language.Node;

public class Context {
    protected Node<?> source;
    protected Context parent;
    protected String base = null;
    /** A stack of prefix maps is built from nesting using the PrefixMap2 class */
    protected PrefixMap localPrefixMap = null;

    protected String iri;
    protected String ns;

    protected PrefixMap finalPrefixMap = null;
    protected String finalBase = null;
    protected String finalNs = null;
    protected String finalIri = null;

    public Context(Context parent, Node<?> source) {
        super();
        this.parent = parent;
        this.source = source;
    }

    public Context getParent() {
        return parent;
    }

    public Optional<Context> tryGetParent() {
        return Optional.ofNullable(parent);
    }

    public void setBase(String iri) {
        this.base = iri;
    }

    public String getBase() {
        String result = base != null ? base : (parent != null ? parent.getBase() : null);
        return result;
    }

//    public String getPrefix(String prefix) {
//        String result = localPrefixMap != null ? localPrefixMap.get(prefix) : (parent != null ? parent.getPrefix(prefix) : null);
//        return result;
//    }

    public PrefixMap getLocalPrefixMap() {
        return localPrefixMap;
    }

    public void setLocalPrefixMap(PrefixMap prefixMap) {
        this.localPrefixMap = prefixMap;
        update();
    }

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public String getNs() {
        return ns;
    }

    public void setNs(String ns) {
        this.ns = ns;
    }

    public static <T> T combine(T a, T b, BinaryOperator<T> combiner, Supplier<T> nullCase) {
        T result = a == null
            ? b == null
                ? nullCase.get()
                : b
            : b == null
                ? a
                : combiner.apply(a, b);
        return result;
    }

    public void update() {
        finalPrefixMap = buildFinalPrefixMap();
        PrefixMapping pm = new PrefixMappingAdapter(finalPrefixMap);
        finalBase = base == null ? null : Optional.ofNullable(pm.getNsPrefixURI(base)).orElseGet(() -> pm.expandPrefix(base));
        finalNs = ns == null ? null : Optional.ofNullable(pm.getNsPrefixURI(ns)).orElseGet(() -> pm.expandPrefix(ns));
        finalIri = iri == null ? null : Optional.ofNullable(pm.getNsPrefixURI(iri)).orElseGet(() -> pm.expandPrefix(iri));
    }

    public PrefixMap buildFinalPrefixMap() {
        PrefixMap parentMap = parent == null ? null : parent.getFinalPrefixMap();
        PrefixMap result = combine(parentMap, localPrefixMap, PrefixMap2::new, PrefixMapFactory::emptyPrefixMap);
        return result;
    }

    public PrefixMap getFinalPrefixMap() {
        return finalPrefixMap;
    }

    public String getFinalBase() {
        return finalBase != null ? finalBase : tryGetParent().map(Context::getFinalBase).orElse(null);
    }

    public String getFinalNs() {
        return finalNs != null ? finalNs : tryGetParent().map(Context::getFinalNs).orElse(null);
    }

    public String getFinalIri() {
        return finalIri != null ? finalIri : tryGetParent().map(Context::getFinalIri).orElse(null);
    }
}

