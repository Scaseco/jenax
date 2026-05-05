package org.aksw.jena_sparql_api.mapper.test.cases;

import java.util.Map;

import org.aksw.jena_sparql_api.mapper.model.TypeDeciderImpl;
import org.apache.jena.graph.Node;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestRdfMapperAnnotationScan {

    @Test
    public void test() {
        Map<Class<?>, Node> map = TypeDeciderImpl.scan("org.aksw.jena_sparql_api.mapper.test");
        Assertions.assertNotEquals(0, map.size());
    }
}
