package org.aksw.jenax.io.kryo.jenax;

import org.aksw.jenax.arq.dataset.api.RDFNodeInDataset;
import org.aksw.jenax.arq.dataset.impl.LiteralInDatasetImpl;
import org.aksw.jenax.arq.dataset.impl.ResourceInDatasetImpl;
import org.aksw.jenax.sparql.relation.dataset.GraphNameAndNode;

import com.esotericsoftware.kryo.Kryo;

/**
 * Additional serializers for jena related classes which are however not part of the official jena.
 */
public class JenaxKryoRegistratorLib {
    public static void registerClasses(Kryo kryo) {
        // kryo.register(NodesInDataset.class, new NodesInDatasetSerializer());
        kryo.register(GraphNameAndNode.class, new GraphNameAndNodeSerializer());

        kryo.register(ResourceInDatasetImpl.class, new RDFNodeInDatasetSerializer<>(RDFNodeInDataset::asResource));
        kryo.register(LiteralInDatasetImpl.class, new RDFNodeInDatasetSerializer<>(RDFNodeInDataset::asLiteral));
    }
}
