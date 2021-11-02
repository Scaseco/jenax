package org.aksw.facete.v3.impl;

import java.util.Optional;

import javax.persistence.Entity;

import org.aksw.commons.util.range.CountInfo;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.jena_sparql_api.data_query.impl.CountUtils;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.utils.views.map.MapVocab;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

import com.google.common.collect.Range;

@Entity // The Entity annotation causes a metamodel class for type-safe property access to become generated during build.
public class FacetValueCountImpl
    extends ResourceImpl
    implements FacetValueCount
{

    public FacetValueCountImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    @Override
    public Node getPredicate() {
        return ResourceUtils.tryGetPropertyValue(this, Vocab.predicate)
                .map(RDFNode::asNode)
                .orElse(null);

        // return this.asNode();
//		return getProperty(RDF.predicate).getObject().asNode();
    }

    @Override
    public Node getValue() {
        return ResourceUtils.tryGetPropertyValue(this, MapVocab.value)
                .map(RDFNode::asNode)
                .orElse(null);
//		return getProperty(Vocab.value).getObject().asNode();
    }

    @Override
    public CountInfo getFocusCount() {
        Long min = Optional.ofNullable(getProperty(Vocab.facetCount)).map(Statement::getLong).orElse(null);
        Long max = min;

//		Long min = Optional.ofNullable(getProperty(OWL.minCardinality)).map(Statement::getLong).orElse(null);
//		Long max = Optional.ofNullable(getProperty(OWL.maxCardinality)).map(Statement::getLong).orElse(null);

        Range<Long> range;
        if (min == null) {
            throw new RuntimeException("Should not happen");
        } else {
            range = max == null ? Range.atLeast(min) : Range.closed(min, max);
        }

        CountInfo result = CountUtils.toCountInfo(range);
        return result;
    }

    @Override
    public String toString() {
        return "FacetValueCountImpl [" + this.getPredicate() + ": " + getValue() + ": " + getFocusCount() + "]";
    }

}
