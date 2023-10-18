package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.aksw.jenax.analytics.core.MappedConcept;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.impl.Concept;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.locationtech.jts.geom.Envelope;

/**
 * Bundles an experssion-based {@link BBoxExprFactory} with a graph pattern based fragment.
 * TODO I don't like the abstraction of mappedConcept (fragment + aggregator, can we have something better?)
 */
public class GeoFragmentFactory {
    protected MappedConcept<?> mappedConcept;
    protected BBoxExprFactory bboxExprFactory;

    public GeoFragmentFactory(MappedConcept<?> mappedConcept, BBoxExprFactory bboxExprFactory) {
        this.mappedConcept = mappedConcept;
        this.bboxExprFactory = bboxExprFactory;
    }

    public MappedConcept<?> createMap(Envelope bounds) {
        var concept = mappedConcept.getConcept();

        var agg = mappedConcept.getAggregator();
        var baseElement = concept.getElement();

        var element = baseElement;
        if (bounds != null) {
            var filterExpr = bboxExprFactory.createExpr(bounds);
            var filterElement = new ElementFilter(filterExpr);

            element = ElementUtils.groupIfNeeded(baseElement, filterElement);
        }

        Fragment1 c = new Concept(element, concept.getVar());
        MappedConcept<?> result = new MappedConcept(c, agg);
        return result;
    }
}
