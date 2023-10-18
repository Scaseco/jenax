package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.aksw.commons.rx.lookup.MapPaginator;
import org.aksw.commons.rx.lookup.MapService;
import org.aksw.jena_sparql_api.lookup.MapServiceSparqlQuery;
import org.aksw.jenax.analytics.core.MappedConcept;
import org.aksw.jenax.analytics.core.MappedEntityFragment;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.apache.jena.graph.Node;
import org.locationtech.jts.geom.Envelope;


/**
 * A simple map service that filters a graph pattern to those rows that are within the requested
 * bounding boxes.
 */
public class ListServiceBBox<T>
    implements MapService<Envelope, Node, T>
{
    protected RdfDataSource dataSource;
    protected GeoFragmentFactory geoFragmentFactory;
    protected MappedEntityFragment<T> mappedEntityFragment;

    public ListServiceBBox(RdfDataSource dataSource, GeoFragmentFactory geoFragmentFactory, MappedEntityFragment<T> mappedEntityFragment) {
        this.dataSource = dataSource;
        this.geoFragmentFactory = geoMapFactory;
        this.mappedEntityFragment = mappedEntityFragment;
        // this.fnGetBBox = fnGetBBox || defaultDocWktExtractorFn;
        // TODO How to augment the data provided by the geoMapFactory?
    }


    @Override
    public MapPaginator<Node, T> createPaginator(Envelope bounds) {
        Fragment1 concept = mappedEntityFragment.getEntityFragment().getFragment().toFragment1();
        MappedConcept<?> constraint = geoMapFactory.createMap(bounds);

        Fragment1 combined = constraint.getConcept().join().with(concept).toFragment1();

        ms = new MapServiceSparqlQuery<>();

//        return new MapPaginator<Node, T>() {
//            @Override
//            public Flowable<Entry<Node, T>> apply(Range<Long> t) {
//
//            }
//
//            @Override
//            public Single<Range<Long>> fetchCount(Long itemLimit, Long rowLimit) {
//            }
//        };
    }

}
