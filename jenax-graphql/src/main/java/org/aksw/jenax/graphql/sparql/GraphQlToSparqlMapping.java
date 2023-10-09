package org.aksw.jenax.graphql.sparql;

import java.util.LinkedHashMap;
import java.util.Map;

import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.io.json.mapper.RdfToJsonMapper;

import graphql.language.Document;
import graphql.language.Field;

public class GraphQlToSparqlMapping {
    public static class Entry {
        protected Field topLevelField;
        protected NodeQuery nodeQuery;
        protected RdfToJsonMapper mapper;

        public Entry(Field topLevelField, NodeQuery nodeQuery, RdfToJsonMapper mapper) {
            super();
            this.nodeQuery = nodeQuery;
            this.mapper = mapper;
            this.topLevelField = topLevelField;
        }

        public NodeQuery getNodeQuery() {
            return nodeQuery;
        }

        public RdfToJsonMapper getMapper() {
            return mapper;
        }

        public Field getTopLevelField() {
            return topLevelField;
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

    public void addEntry(Field topLevelField, NodeQuery nodeQuery, RdfToJsonMapper mapper) {
        String fieldName = topLevelField.getName();
        topLevelMappings.put(fieldName, new Entry(topLevelField, nodeQuery, mapper));
    }
}
