package org.aksw.jena_sparql_api.utils.views.map;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

import org.aksw.commons.collections.ConvertingCollection;
import org.aksw.commons.collections.SinglePrefetchIterator;
import org.aksw.commons.collections.sets.SetFromCollection;
import org.aksw.commons.util.convert.ConvertFunction;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.rdf.collections.SetFromPropertyValues;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import com.google.common.base.Converter;

/**
 *
 * A map view for over the values of a specific property of a specific resource,
 * modeled in the following way:
 *
 * :subject
 *   :entryProperty ?entry .
 *
 *  ?entry
 *     :keyProperty ?key
 *     :valProperty ?value
 *
 *  The map associates each ?key with ?value.
 *
 *  Use a converter to convert the value to e.g. a property of ?value
 *  (this way, the map will lose its put capability)
 *
 * @author raven
 *
 */
public class MapFromResource
    extends MapFromResourceBase<RDFNode, RDFNode>
{
    protected final Property entryProperty;
    protected final Property keyProperty;
    protected final Property valueProperty;


    protected BiFunction<Resource, RDFNode, Resource> sAndKeyToEntry;
    //protected fin
    //protected Function<String, Resource> entryResourceFactory;

    public MapFromResource(
            Resource subject,
            Property entryProperty,
            Property keyProperty,
            Property valueProperty)
    {
        this(subject, entryProperty, keyProperty, valueProperty, null, null);
    }

    public MapFromResource(
            Resource subject,
            Property entryProperty,
            Property keyProperty,
            Property valueProperty,
            ConvertFunction<? super RDFNode, RDFNode> keyConverter,
            ConvertFunction<? super RDFNode, RDFNode> valueConverter
            )
    {
        this(subject, entryProperty, keyProperty, valueProperty, keyConverter, valueConverter, (s, k) -> s.getModel().createResource());
    }

    public MapFromResource(
            Resource subject,
            Property entryProperty,
            Property keyProperty,
            Property valueProperty,
            ConvertFunction<? super RDFNode, RDFNode> keyConverter,
            ConvertFunction<? super RDFNode, RDFNode> valueConverter,
            BiFunction<Resource, RDFNode, Resource> sAndKeyToEntry
            ) {
        super(subject, keyConverter, valueConverter);
        this.entryProperty = entryProperty;
        this.keyProperty = keyProperty;
        this.valueProperty = valueProperty;

        this.sAndKeyToEntry = sAndKeyToEntry;
    }

    @Override
    public RDFNode get(Object key) {
        Resource entry = key instanceof RDFNode ? getEntry((RDFNode)key) : null;

        RDFNode result = entry == null ? null : ResourceUtils.getPropertyValue(entry, valueProperty);

        return result;
    }

//	public Resource getEntry( key) {
//		Resource result = key instanceof RDFNode ? getEntry((RDFNode)key) : null;
//		return result;
//	}

    public Resource getEntry(RDFNode key) {
//		Stopwatch sw = Stopwatch.createStarted();
        Resource result = getEntryViaModel(key);
//		System.out.println("Elapsed (s): " + sw.stop().elapsed(TimeUnit.NANOSECONDS) / 1000000000.0);

        return result;
    }


    public Resource getEntryViaModel(RDFNode key) {
        Model model = subject.getModel();
        List<Resource> tmp = model.listSubjectsWithProperty(keyProperty, key)
            .filterKeep(e -> model.contains(subject, entryProperty, e))
            .toList(); // ensure that the iterator is closed
            // .nextOptional()
            // .orElse(null);

        Resource result = tmp.isEmpty() ? null : tmp.get(0);

        return result;
    }
/*
    public Resource getEntryViaSparql(RDFNode key) {

        UnaryRelation e = new Concept(
                ElementUtils.createElementTriple(
                        new Triple(Vars.e, keyProperty.asNode(), key.asNode()),
                        new Triple(subject.asNode(), entryProperty.asNode(), Vars.e))
                , Vars.e);

            Query query = RelationUtils.createQuery(e);

            Model model = subject.getModel();

            Resource result = SparqlRx.execSelect(() -> QueryExecutionFactory.create(query, model))
                .map(qs -> qs.get(e.getVar().getName()).asResource())
                .singleElement()
                .blockingGet();

        return result;
    }
*/
    @Override
    public boolean containsKey(Object key) {
        RDFNode r = get(key);
        boolean result = r != null;
        return result;
    }

    @Override
    public Resource put(RDFNode key, RDFNode value) {
        Resource entry = getEntry(key);

        if(entry == null) {
            entry = sAndKeyToEntry.apply(subject, key);
            Objects.requireNonNull(entry);
        }

        //Resource e = entry.inModel(subject.getModel());

//		if(!Objects.equals(existing, entry)) {
//			if(existing != null) {
//				subject.getModel().remove(subject, entryProperty, existing);
//			}
//		}

        subject.addProperty(entryProperty, entry);

        ResourceUtils.setProperty(entry, keyProperty, key);
        ResourceUtils.setProperty(entry, valueProperty, value);

        return entry;
    }


    @Override
    public Set<Entry<RDFNode, RDFNode>> entrySet() {
        Converter<Resource, Entry<RDFNode, RDFNode>> converter = Converter.from(
                e -> new RdfEntryWithCast(new RdfEntryKv(e.asNode(), (EnhGraph)e.getModel(), entryProperty, keyProperty, valueProperty), keyConverter, valueConverter),
                e -> (Resource)e); // TODO Ensure to add the resource and its key to the subject model

        Set<Entry<RDFNode, RDFNode>> result =
            new SetFromCollection<>(
                new ConvertingCollection<>(
                    new SetFromPropertyValues<Resource>(subject, entryProperty, Resource.class) {
                        public Iterator<Resource> iterator() {
                            Iterator<Resource> baseIt = super.iterator();

                            return new SinglePrefetchIterator<Resource>() {
                                @Override
                                protected Resource prefetch() throws Exception {
                                    return baseIt.hasNext() ? baseIt.next() : finish();
                                }

                                protected void doRemove(Resource item) {
                                    item.removeAll(keyProperty);
                                    item.removeAll(valueProperty);
                                    baseIt.remove();
                                };

                            };
                        };
//						@Override
//						public void clear() {
//							System.out.println("here");
//							super.clear();
//						}
//						@Override
//						public boolean remove(Object key) {
//							boolean r = super.remove(key);
//							if(r) {
//								((RdfEntry)key).clear();
//							}
//							return r;
//						}
                    },
                    converter));

        return result;
    }

//	@Override
//	public void clear() {
//		for(Object r : entrySet()) {
//			RdfEntry e = (RdfEntry)r;
//			e.clear();
//		}
//
//		super.clear();
//	}

}
