package org.aksw.jenax.dataaccess.rx;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.aksw.jenax.sparql.fragment.impl.ConceptUtils;
import org.aksw.jenax.sparql.fragment.impl.FragmentUtils;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

import com.google.common.collect.Maps;

import io.reactivex.rxjava3.core.Flowable;

public class MapFromBinaryRelation
    extends AbstractMap<RDFNode, Collection<RDFNode>>
{
    protected Model model;
    protected Fragment2 relation;

    // TODD Maybe a Plan object would be a better basis for the map - the plan would
    // already be precomputed and consider all optimizations


    public MapFromBinaryRelation(Model model, Fragment2 relation) {
        super();
        this.model = model;
        this.relation = relation;
    }

//	public <K, V> Map<K, V> transform(Function<Model, Converter<RDFNode, K>> keyMapper) {
//
//	}

    @Override
    public Collection<RDFNode> get(Object key) {
        Collection<RDFNode> result = null;
        if(key instanceof RDFNode) {
            RDFNode k = (RDFNode)key;
            Fragment2 br = relation.joinOn(relation.getSourceVar()).with(ConceptUtils.createFilterConcept(k.asNode())).toFragment2();

            result = Optional.ofNullable(fetch(model, br)).map(f -> f.map(Entry::getValue).toList().blockingGet()).orElse(null);
        }

        return result;
    }

    @Override
    public boolean containsKey(Object key) {
        Collection<RDFNode> item = get(key);
        boolean result = item != null;
        return result;
    }

    @Override
    public Set<Entry<RDFNode, Collection<RDFNode>>> entrySet() {

        Map<RDFNode, Collection<RDFNode>> map = fetch(model, relation)
            .toMultimap(Entry::getKey, Entry::getValue) //, LinkedHashMap::new, LinkedHashSet::new)
            .blockingGet();

        return map.entrySet();
    }

    public static Flowable<Entry<RDFNode, RDFNode>> fetch(Model model, Fragment2 relation) {
        Query query = FragmentUtils.createQuery(relation);

        Flowable<Entry<RDFNode, RDFNode>> result = SparqlRx.execSelect(() -> QueryExecutionFactory.create(query, model))
            .map(qs -> Maps.immutableEntry(
                    qs.get(relation.getSourceVar().getName()),
                    qs.get(relation.getTargetVar().getName())));

        return result;
    }
}
