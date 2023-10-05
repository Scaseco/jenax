package org.aksw.jena_sparql_api.shape.lookup;

import org.aksw.commons.rx.lookup.MapPaginator;
import org.aksw.commons.rx.lookup.MapService;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.shape.ResourceShape;
import org.aksw.jenax.analytics.core.MappedConcept;
import org.aksw.jenax.dataaccess.sparql.execution.factory.query.QueryExecutionFactoryQuery;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;

public class MapServiceResourceShapeDataset
    implements MapService<Concept, Node, DatasetGraph>
{
    private QueryExecutionFactoryQuery qef;
    private ResourceShape resourceShape;
    private boolean isLeftJoin;

    public MapServiceResourceShapeDataset(QueryExecutionFactoryQuery qef,
            ResourceShape resourceShape,
            boolean isLeftJoin) {
        super();
        this.qef = qef;
        this.resourceShape = resourceShape;
        this.isLeftJoin = isLeftJoin;
    }

    @Override
    public MapPaginator<Node, DatasetGraph> createPaginator(Concept filterConcept) {
        MappedConcept<DatasetGraph> mappedConcept = ResourceShape.createMappedConcept2(resourceShape, filterConcept, false);
        MapPaginatorMappedConcept<DatasetGraph> result = new MapPaginatorMappedConcept<>(qef, filterConcept, isLeftJoin, mappedConcept);
        return result;
    }

    public static MapServiceResourceShapeDataset create(QueryExecutionFactoryQuery qef, ResourceShape resourceShape, boolean isLeftJoin) {
        MapServiceResourceShapeDataset result = new MapServiceResourceShapeDataset(qef, resourceShape, isLeftJoin);
        return result;
    }
}
