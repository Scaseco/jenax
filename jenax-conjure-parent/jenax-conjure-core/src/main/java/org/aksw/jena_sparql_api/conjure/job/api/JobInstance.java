package org.aksw.jena_sparql_api.conjure.job.api;

import java.util.Map;

import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.noderef.NodeRef;
import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

/**
 * A Job instance binds the placeholders/variables
 * of a job to concrete values
 *
 * @author raven
 *
 */
@ResourceView
@RdfTypeNs("rpif")
@HashId
public interface JobInstance
    extends Resource
{
    @IriNs("rpif")
    @HashId
    Job getJob();
    JobInstance setJob(Job job);

    @IriNs("rpif")
    @HashId
    NodeRef getJobRef();
    JobInstance setJobRef(Resource res);

    // These are variables that are substituted with literals
    //Map<String, RDFNode> setEnvMap();
    @IriNs("rpif")
    @HashId
    Map<String, Node> getEnvMap();

    // Mapping of OpVar variables - these are variables that are substituted by sub workflows
    // I.e. their evaluation yields datasets
    // TODO Maybe instead of OpVar - ResourceVar or so would be better
    // so extension points where a resource from another RDF graph (together with that graph)
    // can be injected
    //Map<String, RDFNode> setOpVarMap();
    @IriNs("rpif")
    @HashId
    Map<String, Op> getOpVarMap();


    public static JobInstance create(Job job) {
        return job.getModel().createResource().as(JobInstance.class)
                .setJob(job);
    }
}
