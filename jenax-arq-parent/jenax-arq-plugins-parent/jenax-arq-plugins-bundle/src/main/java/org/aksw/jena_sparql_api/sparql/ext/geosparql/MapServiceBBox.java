package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import java.util.Map;

import org.aksw.commons.rx.lookup.MapPaginator;
import org.aksw.commons.rx.lookup.MapService;
import org.aksw.jena_sparql_api.lookup.MapPaginatorConcept;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngines;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.impl.ConceptUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.locationtech.jts.geom.Envelope;


/**
 * A simple map service that filters a graph pattern to those rows that are within the requested
 * bounding boxes.
 */
public class MapServiceBBox
    implements MapService<Envelope, Node, Node>
{
    protected RdfDataSource dataSource;
    protected GeoConstraintFactory geoConstraintFactory;
    protected Fragment1 concept;

    public MapServiceBBox(RdfDataSource dataSource, Fragment1 concept, GeoConstraintFactory geoConstraintFactory) {
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
        RdfDataSource ds = RdfDataEngines.of(RDFDataMgr.loadDataset("/home/raven/Datasets/fp7_ict_project_partners_database_2007_2011.nt.bz2"));
        // Fragment1 concept = ConceptUtils.createForRdfType("http://fp7-pp.publicdata.eu/ontology/Project");
        Fragment1 concept = ConceptUtils.createSubjectConcept();
        GeoConstraintFactory gcf = GeoConstraintFactoryWgs.create();
        MapService<Envelope, Node, Node> mapService = new MapServiceBBox(ds, concept, gcf);
        Envelope bounds = new Envelope(0, 65, 0, 75);
        MapPaginator<Node, Node> paginator = mapService.createPaginator(bounds);
        System.out.println("Count: " + paginator.fetchCount(null, null).blockingGet());
        Map<?, ?> map = paginator.fetchMap();
        for (Object item : map.keySet()) {
            System.out.println(item);
        }
        System.out.println("Item count: " + map.keySet().size());
    }
}