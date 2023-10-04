package org.aksw.jenax.graphql.impl.sparql;

import java.util.LinkedHashMap;
import java.util.Map;

import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.io.json.mapper.RdfToJsonMapper;

public class GraphQlToSparqlMapping {
    public static class Entry {
        protected NodeQuery nodeQuery;
        protected RdfToJsonMapper mapper;

        public Entry(NodeQuery nodeQuery, RdfToJsonMapper mapper) {
            super();
            this.nodeQuery = nodeQuery;
            this.mapper = mapper;
        }

        public NodeQuery getNodeQuery() {
            return nodeQuery;
        }

        public RdfToJsonMapper getMapper() {
            return mapper;
        }
    }

    protected Map<String, Entry> topLevelMappings;

    public GraphQlToSparqlMapping() {
        this(new LinkedHashMap<>());
    }

    public GraphQlToSparqlMapping(Map<String, Entry> topLevelMappings) {
        super();
        this.topLevelMappings = topLevelMappings;
    }

    public Map<String, Entry> getTopLevelMappings() {
        return topLevelMappings;
    }

    public void addEntry(String name, NodeQuery nodeQuery, RdfToJsonMapper mapper) {
        topLevelMappings.put(name, new Entry(nodeQuery, mapper));
    }
}
