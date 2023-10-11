package org.aksw.jenax.graphql.sparql;

import java.util.LinkedHashMap;
import java.util.Map;

import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.io.json.graph.GraphToJsonMapper;
import org.apache.jena.riot.system.PrefixMap;

import graphql.language.Document;
import graphql.language.Field;

public class GraphQlToSparqlMapping {
    public static class Entry {
        protected Field topLevelField;

        /** A cache of the prefixes computed from the field */
        protected PrefixMap prefixMap;
        protected NodeQuery nodeQuery;
        protected GraphToJsonMapper mapper;

        public Entry(Field topLevelField, PrefixMap prefixMap, NodeQuery nodeQuery, GraphToJsonMapper mapper) {
            super();
            this.nodeQuery = nodeQuery;
            this.prefixMap = prefixMap;
            this.mapper = mapper;
            this.topLevelField = topLevelField;
        }

        public Field getTopLevelField() {
            return topLevelField;
        }

        public PrefixMap getPrefixMap() {
            return prefixMap;
        }

        public NodeQuery getNodeQuery() {
            return nodeQuery;
        }

        public GraphToJsonMapper getMapper() {
            return mapper;
        }
    }

    protected Document document;
    protected Map<String, Entry> topLevelMappings;

    public GraphQlToSparqlMapping(Document document) {
        this(document, new LinkedHashMap<>());
    }

    public GraphQlToSparqlMapping(Document document, Map<String, Entry> topLevelMappings) {
        super();
        this.document = document;
        this.topLevelMappings = topLevelMappings;
    }

    public Document getDocument() {
        return document;
    }

    public Map<String, Entry> getTopLevelMappings() {
        return topLevelMappings;
    }

    public void addEntry(Field topLevelField, PrefixMap prefixMap, NodeQuery nodeQuery, GraphToJsonMapper mapper) {
        String fieldName = topLevelField.getName();
        topLevelMappings.put(fieldName, new Entry(topLevelField, prefixMap, nodeQuery, mapper));
    }
}
