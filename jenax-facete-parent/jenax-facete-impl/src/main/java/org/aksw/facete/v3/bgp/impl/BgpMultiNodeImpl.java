package org.aksw.facete.v3.bgp.impl;

import static org.aksw.facete.v3.api.Direction.BACKWARD;
import static org.aksw.facete.v3.api.Direction.FORWARD;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.aksw.facete.v3.api.Direction;
import org.aksw.facete.v3.bgp.api.BgpMultiNode;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.rdf.collections.SetFromPropertyValues;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

public class BgpMultiNodeImpl
    extends ResourceImpl
    implements BgpMultiNode
{
    public BgpMultiNodeImpl(Node n, EnhGraph m) {
        super(n, m);
//		System.out.println("CREATED " + n);
//		Thread.dumpStack();
//		Thread.currentThread().getStackTrace()
//		new RuntimeException().printStackTrace();
    }

    public static <T> Optional<T> toOptional(Iterable<T> i) {
        Iterator<T> it = i.iterator();

        T first = it.hasNext() ? it.next() : null;

        if(it.hasNext()) {
            throw new RuntimeException("More than 1 item found: " + Iterables.toString(i));
        }

        Optional<T> result = Optional.ofNullable(first);
        return result;
    }

    public static <T> T chainAdd(Collection<? super T> c, T item) {
        c.add(item);
        return item;
    }

    @Override
    public BgpNode one() {
        Set<BgpNode> set = new SetFromPropertyValues<>(this, Vocab.one, BgpNode.class);
        Set<BgpNode> children = new SetFromPropertyValues<>(this, Vocab.child, BgpNode.class);

        BgpNode result = toOptional(set).orElseGet(() -> chainAdd(children, chainAdd(set, getModel().createResource()
                .addProperty(RDF.type, Vocab.BgpNode)
                .as(BgpNode.class))));


        return result;
    }

    @Override
    public boolean contains(BgpNode bgpNode) {
        Set<BgpNode> set = new SetFromPropertyValues<>(this, Vocab.child, BgpNode.class);

        boolean result = set.contains(bgpNode);

        return result;
    }

    @Override
    public BgpNode parent() {
        BgpNode result =
            Optional.ofNullable(
                ResourceUtils.getReversePropertyValue(this, Vocab.fwd, BgpNode.class))
            .orElseGet(() -> ResourceUtils.getReversePropertyValue(this, Vocab.bwd, BgpNode.class));
        return result;
    }


    @Override
    public Property reachingProperty() {
        Property result = ResourceUtils.getPropertyValue(this, Vocab.property, Property.class);
        return result;
    }

    @Override
    public Direction getDirection() {
//		boolean isReverse = false;
//		Resource entry = ResourceUtils.tryGetReversePropertyValue(parent, Vocab.fwd)
//			.orElseGet(() -> ResourceUtils.getReversePropertyValue(parent, Vocab.bwd));

//		System.out.println("THIS: " + this.getId().getLabelString());
//		RDFDataMgr.write(System.out, this.getModel(), RDFFormat.NTRIPLES_UTF8);

        //this.getModel().getGraph().find().forEachRemaining(x -> System.out.println("[" + x.hashCode()+ "] " + x));

        Direction result =
                Optional.ofNullable(
                    ResourceUtils.getReversePropertyValue(this, Vocab.fwd, BgpNode.class))
                    .map(x -> FORWARD)

                .orElseGet(() -> ResourceUtils.tryGetReversePropertyValue(this, Vocab.bwd, BgpNode.class)
                        .map(x -> BACKWARD)
                        .orElseThrow(() -> new IllegalStateException()));
        return result;

    }

    @Override
    public Collection<BgpNode> children() {
        Set<BgpNode> result = new SetFromPropertyValues<>(this, Vocab.child, BgpNode.class);

        return result;
    }

    @Override
    public BgpNode viaAlias(String alias) {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public Map<String, BgpNode> list() {
        throw new RuntimeException("not implemented yet");
    }
}
