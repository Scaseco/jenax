package org.aksw.jena_sparql_api.conjure.plugin;

import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfAuthBasic;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfAuthBearerToken;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRef;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRefCatalog;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRefDcat;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRefEmpty;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRefExt;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRefGit;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRefGraph;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRefOp;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRefUrl;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op1;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op2;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpCoalesce;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpConstruct;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpData;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpError;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpHdtHeader;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpJavaRewrite;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpJavaRewrite.Rewrite;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpJobInstance;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpMacroCall;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpN;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpPersist;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpQueryOverViews;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpSequence;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpSet;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpStmtList;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUnion;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUnionDefaultGraph;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUpdateRequest;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVar;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpWhen;
import org.aksw.jena_sparql_api.conjure.job.api.Job;
import org.aksw.jena_sparql_api.conjure.job.api.JobBinding;
import org.aksw.jena_sparql_api.conjure.job.api.JobInstance;
import org.aksw.jena_sparql_api.conjure.job.api.JobParam;
import org.aksw.jena_sparql_api.conjure.job.api.Macro;
import org.aksw.jena_sparql_api.conjure.job.api.MacroParam;
import org.aksw.jena_sparql_api.conjure.noderef.NodeRef;
import org.aksw.jena_sparql_api.conjure.resourcespec.ResourceSpec;
import org.aksw.jena_sparql_api.conjure.resourcespec.ResourceSpecInline;
import org.aksw.jena_sparql_api.conjure.resourcespec.ResourceSpecUrl;
import org.aksw.jena_sparql_api.conjure.traversal.api.OpPropertyPath;
import org.aksw.jena_sparql_api.conjure.traversal.api.OpTraversal;
import org.aksw.jena_sparql_api.conjure.traversal.api.OpTraversal0;
import org.aksw.jena_sparql_api.conjure.traversal.api.OpTraversal1;
import org.aksw.jena_sparql_api.conjure.traversal.api.OpTraversal2;
import org.aksw.jena_sparql_api.conjure.traversal.api.OpTraversalSelf;
import org.aksw.jena_sparql_api.io.hdt.JenaPluginHdt;
import org.aksw.jena_sparql_api.utils.turtle.TurtleWriterNoBase;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginConjure
    implements JenaSubsystemLifecycle {

    public void start() {
        init();
    }

    @Override
    public void stop() {
    }


    public static void init() {
        TurtleWriterNoBase.register();
        JenaPluginHdt.init();

        JenaPluginUtils.registerResourceClasses(
                Job.class, JobParam.class, JobBinding.class, JobInstance.class, Macro.class,
                MacroParam.class);

        JenaPluginUtils.registerResourceClasses(
                OpTraversal.class, OpTraversal0.class, OpTraversal1.class,
                OpTraversal2.class, OpTraversalSelf.class, OpPropertyPath.class);

        // JenaPluginUtils.registerResourceClasses(RdfEntityInfoDefault.class);
        // JenaPluginUtils.registerResourceClasses(Checksum.class);

        JenaPluginUtils.registerResourceClasses(
                RdfDataRef.class, RdfDataRefCatalog.class, RdfDataRefDcat.class, RdfDataRefEmpty.class, RdfDataRefExt.class,
                RdfDataRefGit.class, RdfDataRefOp.class, RdfDataRefSparqlEndpoint.class, RdfDataRefUrl.class, RdfDataRefGraph.class);

        JenaPluginUtils.registerResourceClasses(
                org.aksw.jena_sparql_api.conjure.entity.algebra.Op.class,
                org.aksw.jena_sparql_api.conjure.entity.algebra.Op0.class,
                org.aksw.jena_sparql_api.conjure.entity.algebra.Op1.class,
                org.aksw.jena_sparql_api.conjure.entity.algebra.OpCode.class,
                org.aksw.jena_sparql_api.conjure.entity.algebra.OpConvert.class,
                org.aksw.jena_sparql_api.conjure.entity.algebra.OpPath.class,
                org.aksw.jena_sparql_api.conjure.entity.algebra.OpValue.class
                );

        JenaPluginUtils.registerResourceClasses(
                Op.class,
                Op1.class,
                Op2.class,
                OpCoalesce.class,
                OpConstruct.class,
                OpUnionDefaultGraph.class,

                OpJavaRewrite.class,
                Rewrite.class,

                OpData.class,
                OpDataRefResource.class,
                OpError.class,
                OpHdtHeader.class,

                NodeRef.class,
                OpJobInstance.class,
                OpMacroCall.class,
                OpN.class,
//        		OpNothing.class,
                OpPersist.class,
                OpQueryOverViews.class,
                OpSequence.class,
                OpSet.class,
                OpStmtList.class,
                OpUnion.class,
                OpUpdateRequest.class,
                OpVar.class,
                OpWhen.class
                );

        JenaPluginUtils.registerResourceClasses(
                ResourceSpec.class, ResourceSpecInline.class, ResourceSpecUrl.class);

        JenaPluginUtils.registerResourceClasses(
                // RdfAuth.class,
                RdfAuthBasic.class,
                RdfAuthBearerToken.class);
    }
}
