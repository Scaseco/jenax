package org.aksw.jenax.dataaccess.rx;

import java.util.function.Function;

import org.aksw.commons.rx.lookup.ListPaginator;
import org.aksw.commons.rx.lookup.ListService;
import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.commons.rx.lookup.MapService;
import org.aksw.commons.rx.lookup.MapServiceFromListService;
import org.aksw.jena_sparql_api.rx.entity.engine.EntityQueryRx;
import org.aksw.jena_sparql_api.rx.entity.engine.EntityQueryRx.EntityQueryProcessed;
import org.aksw.jena_sparql_api.rx.entity.engine.EntityQueryRxBuilder;
import org.aksw.jena_sparql_api.rx.entity.model.AttributeGraphFragment;
import org.aksw.jena_sparql_api.rx.entity.model.EntityBaseQuery;
import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryBasic;
import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryImpl;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.impl.ConceptUtils;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.RDFNode;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class ListServiceEntityQuery
    implements ListService<EntityBaseQuery, RDFNode>
{
    protected QueryExecutionFactoryQuery qef;
    protected AttributeGraphFragment attributePart;

    public ListServiceEntityQuery(QueryExecutionFactoryQuery qef, AttributeGraphFragment attributePart) {
        super();
        this.qef = qef;
        this.attributePart = attributePart;
    }


    @Override
    public ListPaginator<RDFNode> createPaginator(EntityBaseQuery baseQuery) {
        return new ListPaginatorEntityQuery(baseQuery);
    }


    public class ListPaginatorEntityQuery
        implements ListPaginator<RDFNode>
    {
        protected EntityBaseQuery baseQuery;

        public ListPaginatorEntityQuery(EntityBaseQuery baseQuery) {
            super();
            this.baseQuery = baseQuery;
        }

        @Override
        public Flowable<RDFNode> apply(Range<Long> t) {
            t = t == null ? Range.atLeast(0l) : t;

            EntityBaseQuery clone = baseQuery.cloneQuery();

            // TODO Ensure we create a deep clone
            Query standardQuery = clone.getStandardQuery();

            Range<Long> baseRange = QueryUtils.toRange(baseQuery.getStandardQuery());
            Range<Long> effectiveRange = QueryUtils.subRange(baseRange, t);

            QueryUtils.applyRange(standardQuery, effectiveRange);


            EntityQueryImpl entityQuery = new EntityQueryImpl();
            entityQuery.setBaseQuery(clone);
            entityQuery.setAttributePart(attributePart);


            // QueryUtils.applySlice(query, offset, limit, cloneOnChange)

            Flowable<RDFNode> result = EntityQueryRxBuilder.create()
                    .setQueryExecutionFactory(qef)
                    .setQuery(entityQuery)
                    .build();

            return result;
        }

        @Override
        public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {
            EntityQueryImpl entityQuery = new EntityQueryImpl();
            entityQuery.getAttributePart().getMandatoryJoins().addAll(attributePart.getMandatoryJoins());
            entityQuery.setBaseQuery(baseQuery);

            EntityQueryBasic basic = EntityQueryRx.assembleEntityAndAttributeParts(entityQuery);
            EntityQueryProcessed processed = EntityQueryRx.processEntityQuery(basic, true);
            Query query = processed.getInnerSelect();

            // Entry<Var, Query> countData = QueryGenerationUtils.createQueryCount(query);

            Single<Range<Long>> result = SparqlRx.fetchCountQueryPartition(qef, query, processed.getPartitionVars(), itemLimit, rowLimit);
            return result;
        }
    }

    public MapService<EntityBaseQuery, Node, RDFNode> asMapService() {
        MapService<EntityBaseQuery, Node, RDFNode> result = new MapServiceFromListService<>(
                this, RDFNode::asNode, Function.<RDFNode>identity());

        return result;
    }

    public LookupService<Node, RDFNode> asLookupService() {
        MapServiceFromListService<EntityBaseQuery, RDFNode, Node, RDFNode> mapService = new MapServiceFromListService<>(
                this, RDFNode::asNode, Function.<RDFNode>identity());


        LookupService<Node, RDFNode> result = mapService.asLookupService(ListServiceEntityQuery::toBaseQuery);
        return result;
    }

    public static EntityBaseQuery fromConcept(Fragment1 rel) {
        return EntityBaseQuery.create(rel.getVar(), rel.asQuery());
    }

    public static EntityBaseQuery toBaseQuery(Iterable<? extends Node> nodes) {
        Fragment1 concept = ConceptUtils.createConcept(nodes);
        EntityBaseQuery result = fromConcept(concept);
        return result;
    }
}

