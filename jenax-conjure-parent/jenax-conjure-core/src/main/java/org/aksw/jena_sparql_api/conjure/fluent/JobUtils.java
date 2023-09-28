package org.aksw.jena_sparql_api.conjure.fluent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpUtils;
import org.aksw.jena_sparql_api.conjure.job.api.Job;
import org.aksw.jena_sparql_api.conjure.job.api.JobInstance;
import org.aksw.jena_sparql_api.conjure.job.api.JobParam;
import org.aksw.jena_sparql_api.conjure.resourcespec.RpifTerms;
import org.aksw.jenax.arq.util.node.NodeEnvsubst;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtParserImpl;
import org.aksw.jenax.stmt.util.SparqlStmtUtils;
import com.google.common.collect.Streams;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.apache.jena.vocabulary.RDF;

import com.google.common.collect.Iterables;

public class JobUtils {

    public static List<Job> listJobs(Model model) {
        Resource xjob = ResourceFactory.createResource(RpifTerms.NS + "Job");

        List<Job> jobs = model.listResourcesWithProperty(RDF.type, xjob)
                .mapWith(r -> r.as(Job.class))
                .toList();
        return jobs;
    }


    public static Job getOnlyJob(Model model) {
        List<Job> jobs = listJobs(model);
        Job job = Iterables.getOnlyElement(jobs);
        return job;
    }

    
    /**
     * Create a job to derive a new dataset using a set of sparql construct statements.
     * (Update statements could be made part of the workflow but are not supported by this method yet)
     * 
     * The OpVar placeholder for the input dataset is "ARG".
     * 
     * @param stmts
     * @param optionalArgs
     * @param varToExpr
     * @return
     */
    public static Job fromSparqlStmts(
            Collection<SparqlStmt> stmts,
            Set<String> optionalArgs,
            Map<Var, Expr> varToExpr
        ) {


        Set<String> mentionedEnvVars = SparqlStmtUtils.getMentionedEnvVars(stmts);

// TODO Add API for Query objects to fluent
//		List<SparqlStmt> stmts = Streams.stream(SparqlStmtUtils.processFile(DefaultPrefixes.prefixes, path))
//				.collect(Collectors.toList());

        List<String> stmtStrs = stmts.stream()
                .map(Object::toString)
                .collect(Collectors.toList());

//
//		List<String> queries = RDFDataMgrEx.loadQueries(path, DefaultPrefixes.prefixes).stream()
//				.map(Object::toString)
//				.collect(Collectors.toList());
        ConjureBuilder cj = new ConjureBuilderImpl();

        String opVarName = "ARG";
//        Op op = cj.fromVar(opVarName).stmts(stmtStrs).getOp();
        Op op = cj.fromVar(opVarName).construct(stmtStrs).getOp();

//		Set<String> vars = OpUtils.mentionedVarNames(op);
//		for(SparqlStmt stmt : stmts) {
//			System.out.println("Env vars: " + SparqlStmtUtils.mentionedEnvVars(stmt));
//		}

        Map<String, Boolean> combinedMap = stmts.stream()
            .map(SparqlStmtUtils::mentionedEnvVars)
            .map(Map::entrySet)
            .flatMap(Collection::stream)
            .distinct()
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        Set<String> envVars = combinedMap.keySet();
//		System.out.println("All env vars: " + combinedMap);


//		System.out.println("MentionedVars: " + vars);

        Job result = Job.create(cj.getContext().getModel())
                .setOp(op)
                // .setDeclaredVars(envVars)
                .setOpVars(Collections.singleton(opVarName));

        for (String varName : mentionedEnvVars) {
            JobParam param = result.addNewParam();
            param.setParamName(varName);

            Var v = Var.alloc(varName);
            Expr expr = varToExpr.get(v);
            param.setDefaultValueExpr(expr);
        }
        // result.setDeclaredVars(mentionedEnvVars);


        return result;
    }

    public static Job fromSparqlFile(String path) throws FileNotFoundException, IOException, ParseException {
        // TODO Add API for Query objects to fluent
        List<SparqlStmt> stmts = Streams.stream(SparqlStmtUtils.processFile(DefaultPrefixes.get(), path))
                .collect(Collectors.toList());

        List<String> stmtStrs = stmts.stream()
                .map(Object::toString)
                .collect(Collectors.toList());


        //RDFDataMgrRx
        //SparqlStmtUtils.


//
//		List<String> queries = RDFDataMgrEx.loadQueries(path, DefaultPrefixes.prefixes).stream()
//				.map(Object::toString)
//				.collect(Collectors.toList());
        ConjureBuilder cj = new ConjureBuilderImpl();

        String opVarName = "ARG";
        Op op = cj.fromVar(opVarName).stmts(stmtStrs).getOp();

//		Set<String> vars = OpUtils.mentionedVarNames(op);
//		for(SparqlStmt stmt : stmts) {
//			System.out.println("Env vars: " + SparqlStmtUtils.mentionedEnvVars(stmt));
//		}

        Map<String, Boolean> combinedMap = stmts.stream()
            .map(SparqlStmtUtils::mentionedEnvVars)
            .map(Map::entrySet)
            .flatMap(Collection::stream)
            .distinct()
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        Set<String> envVars = combinedMap.keySet();
//		System.out.println("All env vars: " + combinedMap);


//		System.out.println("MentionedVars: " + vars);

        Job result = Job.create(cj.getContext().getModel())
                .setOp(op)
                // .setDeclaredVars(envVars)
                .setOpVars(Collections.singleton(opVarName));

        // Set<JobParam> params = result.getParams();
        for (String varName : envVars) {
            JobParam param = result.addNewParam();
            param.setParamName(varName);
        }

        return result;
    }

    public static JobInstance createJobInstanceWithCopy(
            Job job,
            Map<String, ? extends Node> env,
            Map<String, ? extends Op> map) {
        Model model = ModelFactory.createDefaultModel();
        Job j = JenaPluginUtils.copyClosureInto(job, Job.class, model);

        JobInstance result = model.createResource().as(JobInstance.class)
                .setJob(j);

        result.getEnvMap().putAll(env);

        for(Entry<String, ? extends Op> e : map.entrySet()) {
            String k = e.getKey();
            Op v = e.getValue();

            Op vv = JenaPluginUtils.copyClosureInto(v, Op.class, model);
            result.getOpVarMap().put(k, vv);
        }

        return result;
    }

    /**
     * Create a job instance in the same model as the job
     *
     * @param job
     * @param env
     * @param map
     * @return
     */
    public static JobInstance createJobInstance(
            Job job,
            Map<String, ? extends Node> env,
            Map<String, ? extends Op> map) {
        Model model = job.getModel();
        JobInstance result = model.createResource().as(JobInstance.class)
                .setJob(job);

        result.getEnvMap().putAll(env);

        for(Entry<String, ? extends Op> e : map.entrySet()) {
            String k = e.getKey();
            Op v = e.getValue();

            Op vv = JenaPluginUtils.copyClosureInto(v, Op.class, model);
            result.getOpVarMap().put(k, vv);
        }

        return result;
    }

    /**
     * Return the associated op with all all variables (literals and resources) substituted
     *
     * @param jobInstance
     * @return
     */
    public static Op materializeJobInstance(JobInstance jobInstance) {
        Map<String, Node> envMap = jobInstance.getEnvMap();
        Map<String, Op> opMap = jobInstance.getOpVarMap();

        Job job = jobInstance.getJob();
        Op tmp = job.getOp();
        Op op = JenaPluginUtils.reachableClosure(tmp, Op.class);

        NodeTransform nodeTransform = x -> NodeEnvsubst.substWithNode(x, envMap::get);
        //NodeTransform nodeTransform = new NodeTransformRenameMap(envMap);
        OpUtils.applyNodeTransform(op, nodeTransform, stmt -> SparqlStmtUtils.optimizePrefixes(SparqlStmtParserImpl.create(DefaultPrefixes.get()).apply(stmt)));

        // OpUtils.applyNodeTransform();


        //ResourceUtils.reachableClosure(root)

        Op inst = OpUtils.substituteVars(op, opMap::get);

        return inst;
    }
}
