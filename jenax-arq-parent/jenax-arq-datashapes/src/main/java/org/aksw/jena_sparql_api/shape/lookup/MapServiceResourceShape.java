package org.aksw.jena_sparql_api.shape.lookup;

import org.aksw.commons.rx.lookup.ListService;
import org.aksw.commons.rx.lookup.ListServiceMapWrapper;
import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.commons.rx.lookup.MapPaginator;
import org.aksw.commons.rx.lookup.MapService;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.lookup.LookupServiceListService;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.aksw.jenax.analytics.core.MappedConcept;
import org.aksw.jenax.connection.query.QueryExecutionFactoryQuery;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

public class MapServiceResourceShape
    implements MapService<Concept, Node, Graph>
{
    private QueryExecutionFactoryQuery qef;
    private ResourceShape resourceShape;
    private boolean isLeftJoin;

    public MapServiceResourceShape(QueryExecutionFactoryQuery qef,
            ResourceShape resourceShape,
            boolean isLeftJoin) {
        super();
        this.qef = qef;
        this.resourceShape = resourceShape;
        this.isLeftJoin = isLeftJoin;
    }

    @Override
    public MapPaginator<Node, Graph> createPaginator(Concept filterConcept) {
        MappedConcept<Graph> mappedConcept = ResourceShape.createMappedConcept(resourceShape, filterConcept, false);
        MapPaginatorMappedConcept<Graph> result = new MapPaginatorMappedConcept<>(qef, null, isLeftJoin, mappedConcept);
        return result;
    }

    public static MapServiceResourceShape create(QueryExecutionFactoryQuery qef, ResourceShape resourceShape, boolean isLeftJoin) {
        MapServiceResourceShape result = new MapServiceResourceShape(qef, resourceShape, isLeftJoin);
        return result;
    }



    public static ListService<Concept, Resource> createListService(QueryExecutionFactoryQuery qef, ResourceShape resourceShape, boolean isLeftJoin) {
        MapServiceResourceShape base = create(qef, resourceShape, isLeftJoin);

        ListService<Concept, Resource> result = ListServiceMapWrapper.create(base, ResourceUtils::asResource);

//        (node, graph) -> {
//            Model model = ModelFactory.createModelForGraph(graph);
//            Resource r = ModelUtils.convertGraphNodeToRDFNode(node, model).asResource();
//            return r;
//        });

        return result;
    }


    /**
     * Create a lookup service that wraps an instance of this service
     *
     * @param qef
     * @param shape
     * @return
     */
    public static LookupService<Node, Graph> createLookupService(QueryExecutionFactoryQuery qef, ResourceShape shape) {
        MapServiceResourceShape base = new MapServiceResourceShape(qef, shape, false);
        LookupService<Node, Graph> result = LookupServiceListService.create(base);

        return result;
    }
}
