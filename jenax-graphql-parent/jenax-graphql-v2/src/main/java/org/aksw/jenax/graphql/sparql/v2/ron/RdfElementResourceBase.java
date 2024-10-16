package org.aksw.jenax.graphql.sparql.v2.ron;

import java.util.Objects;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

/** Base class for RDF elements which correspond to resources in an RDF graph -
 *  namely objects and arrays. */
public abstract class RdfElementResourceBase
    extends RdfElementNodeBase
    implements RdfElementResource
{
    public RdfElementResourceBase(Node externalId) {
        this(NodeFactory.createBlankNode(), externalId);
    }

    public RdfElementResourceBase(Node internalId, Node externalId) {
        super(internalId);
        this.externalId = externalId;
    }

    protected Node externalId;

    @Override
    public Node getExternalId() {
        return externalId;
    }

    public void setExternalId(Node internalId) {
        Objects.requireNonNull(internalId);
        this.externalId = internalId;
    }

    /** Return the node; unless it is null then return the internal id. */
    public Node getEffectiveNode() {
        return internalId == null ? externalId : internalId;
    }
}
