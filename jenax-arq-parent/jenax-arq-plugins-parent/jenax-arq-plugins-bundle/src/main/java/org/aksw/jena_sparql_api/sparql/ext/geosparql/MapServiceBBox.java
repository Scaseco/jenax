package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.aksw.commons.rx.lookup.MapPaginator;
import org.aksw.commons.rx.lookup.MapService;
import org.aksw.jena_sparql_api.lookup.MapPaginatorConcept;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngines;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.impl.ConceptUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.vocabulary.XSD;
import org.locationtech.jts.geom.Envelope;


/**
 * A simple map service that filters a graph pattern to those rows that are within the requested
 * bounding boxes.
 */
public class MapServiceBBox
    implements MapService<Envelope, Node, Node>
{
    protected RDFDataSource dataSource;
    protected GeoConstraintFactory geoConstraintFactory;
    protected Fragment1 concept;

    public MapServiceBBox(RDFDataSource dataSource, Fragment1 concept, GeoConstraintFactory geoConstraintFactory) {
        this.dataSource = dataSource;
        this.concept = concept;
        this.geoConstraintFactory = geoConstraintFactory;
    }

    @Override
    public MapPaginator<Node, Node> createPaginator(Envelope bounds) {
        Fragment geoFragment = geoConstraintFactory.getFragment();
        if (bounds != null) {
            Expr expr = geoConstraintFactory.createExpr(bounds);
            geoFragment = geoFragment.filter(expr);
        }
        Var idVar = geoConstraintFactory.getIdVar();
        // TODO We need to project the bindings so we can access x and y
        Fragment1 combined = geoFragment.prependOn(idVar).with(concept).project(idVar).toFragment1();
        return new MapPaginatorConcept(dataSource.asQef(), combined);
    }

    public static void main(String[] args) {
        RDFDataSource ds = RdfDataEngines.of(RDFDataMgr.loadDataset("/home/raven/Datasets/coypu/events.coypu.10000.ttl"));
        System.out.println("data loaded.");

        // RdfDataSource ds = RdfDataEngines.of(RDFDataMgr.loadDataset("/home/raven/Datasets/fp7_ict_project_partners_database_2007_2011.nt.bz2"));
        // Fragment1 concept = ConceptUtils.createForRdfType("http://fp7-pp.publicdata.eu/ontology/Project");
        Fragment1 concept = ConceptUtils.createSubjectConcept();
        GeoConstraintFactory gcf = GeoConstraintFactoryWgs.of("https://schema.coypu.org/global#hasLatitude", "https://schema.coypu.org/global#hasLongitude", XSD.xfloat.getURI());
        MapService<Envelope, Node, Node> mapService = new MapServiceBBox(ds, concept, gcf);
        Envelope bounds = new Envelope(1, 65, 1, 75);
//        MapPaginator<Node, Node> paginator = mapService.createPaginator(bounds);
//        System.out.println("Count: " + paginator.fetchCount(null, null).blockingGet());
//        Map<?, ?> map = paginator.fetchMap();
//        for (Object item : map.keySet()) {
//            System.out.println(item);
//        }
//        System.out.println("Item count: " + map.keySet().size());

        DataServiceBBoxCache<Node, Node> service = new DataServiceBBoxCache<>(mapService, 1000, 100, 2);
        service.runWorkflow(bounds).forEach(x -> {
            System.out.println(x);
            System.out.println(x.getData());
        });
    }
}
