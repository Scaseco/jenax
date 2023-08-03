package org.aksw.jenax.model.shacl.util;

import java.util.List;

import org.aksw.jenax.arq.util.exec.QueryExecutionUtils;
import org.aksw.jenax.model.shacl.domain.ShNodeShape;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;

import com.google.common.base.StandardSystemProperty;

public class ShUtils {
    /** List all resources that have property shapes without being part of an expression itself */
    public static final Query NodeShapeQuery = QueryFactory.create(String.join(StandardSystemProperty.LINE_SEPARATOR.value(),
            "PREFIX sh: <http://www.w3.org/ns/shacl#>",
            "SELECT DISTINCT ?s {",
            "  ?s (sh:and|sh:or|sh:xone)*/sh:property []",
            "  FILTER NOT EXISTS { [] sh:and|sh:or|sh:xone ?s }",
            "}"));

    public static List<ShNodeShape> listNodeShapes(Model model) {
        List<ShNodeShape> result = QueryExecutionUtils.executeRdfList(
                q -> QueryExecutionFactory.create(q, model), NodeShapeQuery, ShNodeShape.class);
        return result;
    }
}
