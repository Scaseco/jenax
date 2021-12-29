package org.aksw.jena_sparql_api.conjure.noderef;

import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRef;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefUrl;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/** A reference to a node in a foreign dataset referred to by a DataRef */
@ResourceView
public interface NodeRef
    extends Resource
{
    @IriNs("rpif")
    DataRef getDataRef();
    NodeRef setDataRef(Resource dataRef);

    @IriNs("rpif")
    Node getNode();
    NodeRef setNode(Node node);

    @IriNs("rpif")
    Node getGraph();
    NodeRef setGraph(Node node);

//    @Override
//    default NodeRef clone(Model cloneModel, List<Op> subOps) {
//        return this.inModel(cloneModel).as(OpUnionDefaultGraph.class)
//                .setSubOp(subOps.iterator().next());
//    }

    public static NodeRef createForFile(Model model, String fileIri, Node node, Node graph) {
      DataRef dataRef = model.createResource().as(DataRefUrl.class).setDataRefUrl(fileIri);

      NodeRef result = model.createResource().as(NodeRef.class)
        .setDataRef(dataRef)
        .setNode(node)
        .setGraph(graph);

      return result;
    }

}

