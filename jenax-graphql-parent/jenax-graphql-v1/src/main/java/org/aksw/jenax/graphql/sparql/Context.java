package org.aksw.jenax.graphql.sparql;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.commons.path.core.Path;
import org.aksw.commons.path.core.PathStr;
import org.aksw.jenax.arq.util.prefix.PrefixMap2;
import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.graph.PrefixMappingAdapter;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import com.google.common.graph.Traverser;

import graphql.language.Field;

// TODO Try to isolate prefix machinery
//class PrefixCxt {
//    protected String base = null;
//    protected PrefixMap prefixMap = null;
//    protected String iri = null;
//    protected String ns = null;
//
//    public String getBase() {
//        return base;
//    }
//    public void setBase(String base) {
//        this.base = base;
//    }
//    public PrefixMap getPrefixMap() {
//        return prefixMap;
//    }
//    public void setPrefixMap(PrefixMap prefixMap) {
//        this.prefixMap = prefixMap;
//    }
//    public String getIri() {
//        return iri;
//    }
//    public void setIri(String iri) {
//        this.iri = iri;
//    }
//    public String getNs() {
//        return ns;
//    }
//    public void setNs(String ns) {
//        this.ns = ns;
//    }
//
//
//}


/** Context for a GraphQL field. */
// XXX Replace field with selection or node because of fragments?
public class Context {

    // Effective local values
    protected Field field;
    protected Context parent;
    protected String base = null;
    /** A stack of prefix maps is built from nesting using the PrefixMap2 class */
    protected PrefixMap localPrefixMap = null;

    protected String iri;
    protected String ns;

    // Computed effective values taking parent context into account
    protected PrefixMap finalPrefixMap = null;
    protected String finalBase = null;
    protected String finalNs = null;
    protected String finalIri = null;

    // protected FacetPath facetPath = null;
    protected NodeQuery nodeQuery;

    // Contexts of immediate children. Used to reference information of fields by name
    protected Map<String, Context> childContexts = new LinkedHashMap<>();

    protected Cardinality thisCardinality;
    protected Cardinality inheritedCardinality;

    public Context(Context parent, Field field) {
        super();
        this.parent = parent;
        this.field = field;

        if (parent != null) {
            this.inheritedCardinality = parent.getInheritedCardinality();
            this.thisCardinality = parent.getInheritedCardinality();
        } else {
            this.inheritedCardinality = Cardinality.MANY;
            this.thisCardinality = Cardinality.MANY;
        }
    }

    public Path<String> getPath() {
        Path<String> parentPath = parent != null ? parent.getPath() : PathStr.newAbsolutePath();
        Path<String> result = field == null
                ? parentPath
                : parentPath.resolve(field.getName());
        return result;
    }

//    public FacetPath getFacetPath() {
//        return facetPath;
//    }
//
//    public void setFacetPath(FacetPath facetPath) {
//        this.facetPath = facetPath;
//    }

    public NodeQuery getNodeQuery() {
        return nodeQuery;
    }

    public void setNodeQuery(NodeQuery nodeQuery) {
        this.nodeQuery = nodeQuery;
    }

    public Field getField() {
        return field;
    }

    public Context newChildContext(Field field) {
        String fieldName = field.getName();
        Context result = new Context(this, field);
        childContexts.put(fieldName, result);
        return result;
    }

    public Context getParent() {
        return parent;
    }

    public Map<String, Context> getChildContexts() {
        return childContexts;
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
        updatePrefixes();
    }

    public void updatePrefixes() {
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


    public Context findOnlyField(String fieldName) {
        Set<Context> matches = findField(fieldName);
        List<Path<String>> matchingPaths = matches.stream().map(Context::getPath).collect(Collectors.toList());

        Context match;
        if (matches.isEmpty()) {
            throw new NoSuchElementException("Could not resolve field name " + fieldName + " at path " + this.getPath());
        } else if (matches.size() > 1) {
            throw new IllegalArgumentException("Ambiguous resolution. Field name + " + fieldName + " expected to resolve to 1 field. Got " + matchingPaths.size() + " fields: " + matchingPaths);
        } else {
            match = Iterables.getOnlyElement(matches);
        }
        return match;
    }

    public Set<Context> findField(String name) {
        Set<Context> result = Streams.stream(Traverser.<Context>forTree(cxt -> cxt.getChildContexts().values()).depthFirstPreOrder(this))
            .filter(cxt -> {
                Field field = cxt.getField();
                Set<String> names = getEffectiveFieldNames(field);
                boolean r = names.contains(name);
                return r;
            })
            .collect(Collectors.toSet());
        return result;
    }


    public static Set<String> getEffectiveFieldNames(Field field) {
        Set<String> result = getAliases(field);
        if (result.isEmpty()) {
            result = Set.of(field.getName());
        }
        return result;
    }

    // Field has a getAlias() method - can we exploit that for our purpose???
    // No, alias causes output fields to become renamed - we want to introduce a flat name by which a nested field can be referenced
    public static Set<String> getAliases(Field field) {
        // String alias = field.getAlias();
//        Set<String> result = alias == null ? Set.of() : Set.of(alias);
        Set<String> result = field.getDirectives("as").stream()
            .map(d -> GraphQlUtils.getArgValueAsString(d, "name", null)) // TODO Raise exception when field aliases are variables!
            .collect(Collectors.toSet());
        return result;
    }

    public Cardinality getThisCardinality() {
        return thisCardinality;
    }

    public void setThisCardinality(Cardinality thisCardinality) {
        this.thisCardinality = thisCardinality;
    }

    public Cardinality getInheritedCardinality() {
        return inheritedCardinality;
    }

    public void setInheritedCardinality(Cardinality inheritedCardinality) {
        this.inheritedCardinality = inheritedCardinality;
    }


    /** Whether the cardinality of the field is effectively single */
//    public Cardinality getFinalCardinality() {
//        Cardinality card = getCardinality();
//        if (card == null && parent != null) {
//            card = parent.getFinalCardinality();
//            if (!card.isAll()) {
//                card = null;
//            }
//        }
//
//        if (card == null) {
//            card = new Cardinality(false, false);
//        }
//        return card;
//    }

//    public static Set<Path<String>> findField(TreeDataMap<Path<String>, Field> tree, Path<String> basePath, String name) {
//        Set<Path<String>> result = Streams.stream(Traverser.<Path<String>>forTree(tree::getChildren).depthFirstPreOrder(basePath))
//            .filter(path -> {
//                Field field = tree.get(path);
//                Set<String> names = getEffectiveFieldNames(field);
//                boolean r = names.contains(name);
//                return r;
//            })
//            .collect(Collectors.toSet());
//        return result;
//    }
}

