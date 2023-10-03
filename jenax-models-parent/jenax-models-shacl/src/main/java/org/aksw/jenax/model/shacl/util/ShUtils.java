package org.aksw.jenax.model.shacl.util;

import java.util.List;
import java.util.stream.Stream;

import org.aksw.jenax.arq.util.exec.QueryExecutionUtils;
import org.aksw.jenax.model.shacl.domain.ShNodeShape;
import org.aksw.jenax.model.shacl.domain.ShPropertyShape;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shacl.engine.ShaclPaths;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.Path;
import org.topbraid.shacl.vocabulary.SH;

import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class ShUtils {
    /** List all resources that have property shapes without being part of an expression itself */
    public static final Query NodeShapeQuery = QueryFactory.create(String.join(StandardSystemProperty.LINE_SEPARATOR.value(),
        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
        "PREFIX sh: <http://www.w3.org/ns/shacl#>",
        "SELECT DISTINCT ?s {",
        "  ?s ((sh:and|sh:or|sh:xone)/rdf:rest*/rdf:first)*/sh:property []",
        "  FILTER NOT EXISTS { [] ((sh:and|sh:or|sh:xone)/rdf:rest*/rdf:first)+ ?s }",
        "}"));

    public static List<ShNodeShape> listNodeShapes(Model model) {
        List<ShNodeShape> result = QueryExecutionUtils.executeRdfList(
                q -> QueryExecutionFactory.create(q, model), NodeShapeQuery, ShNodeShape.class);
        return result;
    }

    public static Stream<ShPropertyShape> streamPropertyShapes(Model model) {
        return  Iter.asStream(model.listResourcesWithProperty(SH.path))
                .map(r -> r.as(ShPropertyShape.class));
    }

    public static Multimap<P_Path0, ShPropertyShape> indexGlobalPropertyShapes(Model model) {
        Multimap<P_Path0, ShPropertyShape> result = LinkedHashMultimap.create();
        try (Stream<ShPropertyShape> stream = streamPropertyShapes(model)) {
            stream.forEach(pshape -> {
                Resource r = pshape.getPath();
                Path path = ShaclPaths.parsePath(r.getModel().getGraph(), r.asNode());
                if (path instanceof P_Path0) {
                    result.put((P_Path0)path, pshape);
                }
            });
        }
        return result;
    }
}
