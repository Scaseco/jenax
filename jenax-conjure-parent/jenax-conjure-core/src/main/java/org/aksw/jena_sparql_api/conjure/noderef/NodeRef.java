package org.aksw.jena_sparql_api.conjure.noderef;

import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRef;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRefDcat;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRefUrl;
import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.PolymorphicOnly;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/** A reference to a node in a foreign dataset referred to by a DataRef */
@ResourceView
public interface NodeRef
    extends Resource
{
    @IriNs("rpif")
    @HashId
    @PolymorphicOnly
    RdfDataRef getDataRef();
    NodeRef setDataRef(Resource dataRef);

    @IriNs("rpif")
    @HashId
    Node getNode();
    NodeRef setNode(Node node);

    @IriNs("rpif")
    @HashId
    Node getGraph();
    NodeRef setGraph(Node node);

//    @Override
//    default NodeRef clone(Model cloneModel, List<Op> subOps) {
//        return this.inModel(cloneModel).as(OpUnionDefaultGraph.class)
//                .setSubOp(subOps.iterator().next());
//    }

    public static NodeRef createForFile(Model model, String fileIri, Node node, Node graph) {
      RdfDataRef dataRef = model.createResource().as(RdfDataRefUrl.class).setDataRefUrl(fileIri);

      NodeRef result = model.createResource().as(NodeRef.class)
        .setDataRef(dataRef)
        .setNode(node)
        .setGraph(graph);

      return result;
    }

    public static NodeRef createForDcatEntity(Model model, Node dcatEntity, Node node, Node graph) {
        RdfDataRef dataRef = model.createResource().as(RdfDataRefDcat.class)
                .setDcatRecordNode(dcatEntity);

        NodeRef result = createForDataRef(model, dataRef.asNode(), node, graph);

        return result;
      }

    public static NodeRef createForDataRef(Model model, Node dataRef, Node node, Node graph) {

        NodeRef result = model.createResource().as(NodeRef.class)
          .setDataRef(model.wrapAsResource(dataRef))
          .setNode(node)
          .setGraph(graph);

        return result;
      }


}

