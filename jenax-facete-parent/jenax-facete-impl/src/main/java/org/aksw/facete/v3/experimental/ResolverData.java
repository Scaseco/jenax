package org.aksw.facete.v3.experimental;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.aksw.facete.v3.api.AliasedPath;
import org.aksw.facete.v3.api.AliasedPathImpl;
import org.aksw.facete.v3.api.path.Resolver;
import org.aksw.facete.v3.api.path.ResolverBase;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.TernaryRelationImpl;
import org.aksw.jena_sparql_api.data_query.api.PathAccessorRdf;
import org.aksw.jena_sparql_api.data_query.impl.PathToRelationMapper;
import org.aksw.jena_sparql_api.data_query.impl.QueryFragment;
import org.aksw.jena_sparql_api.relationlet.RelationletBinary;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.relation.api.BinaryRelation;
import org.aksw.jenax.sparql.relation.api.TernaryRelation;
import org.aksw.jenax.sparql.relation.query.PartitionedQuery1;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;

public class ResolverData
    extends ResolverBase
{
    protected Resolver parent;

    //protected ResolverTemplate base;
    //protected List<P_Path0> steps;
    //protected List<Entry<P_Path0, String>> steps;
    protected BinaryRelation reachingRelation;
    protected AliasedPath path;



    protected PartitionedQuery1 query;
    //protected RDFNode start;


//	public ResolverData(ResolverTemplate base, List<P_Path0> steps) {
    public ResolverData(Resolver parent, PartitionedQuery1 query, AliasedPath path, BinaryRelation reachingRelation) {
        super(parent);
        this.query = query;
        this.path = path;
        this.reachingRelation = reachingRelation;
        //this.base = base;
        //this.steps = steps;
    }



    public Collection<RelationletBinary> getReachingRelationlet() {
        PathToRelationMapper<AliasedPath> mapper = createPathMapper();
        // Allocate the full path
        BinaryRelation tmp = mapper.getOverallRelation(path);

        // Obtain the relation for the last segment of the path
        BinaryRelation result = mapper.getMap().get(path);


        return Collections.singleton(new RelationletBinary(result));
//		// Get the root var
//		Var var = query.getPartitionVar();
//
//
//		// TODO Mark all variables mentioned in the query as forbidden
//		//Set<Var> mentionedVars = null;
//
//		String pathName = "path";
//		String baseName = var.getName() + "_" + pathName;
//
////		PathAccessorRdf<SimplePath> pathAccessor = new PathAccessorSimplePath();
//		PathAccessorRdf<AliasedPath> pathAccessor = new PathAccessorAliasedPath();
//		PathToRelationMapper<AliasedPath> mapper = new PathToRelationMapper<>(pathAccessor, baseName);
//
//		BinaryRelation tmp = reachingRelation == null
//				? new BinaryRelationImpl(new ElementGroup(), var, var)
//				: reachingRelation;
//
//		mapper.getMap().put(AliasedPathImpl.empty(), tmp);
//
//		BinaryRelation result = mapper.getOverallRelation(path);
//
//		return result;
    }

    public PathToRelationMapper<AliasedPath> createPathMapper() {
        // Get the root var
        Var var = query.getPartitionVar();


        // TODO Mark all variables mentioned in the query as forbidden
        //Set<Var> mentionedVars = null;

        String pathName = "path";
        String baseName = var.getName() + "_" + pathName;

//		PathAccessorRdf<SimplePath> pathAccessor = new PathAccessorSimplePath();
        PathAccessorRdf<AliasedPath> pathAccessor = new PathAccessorAliasedPath();
        PathToRelationMapper<AliasedPath> mapper = new PathToRelationMapper<>(pathAccessor, baseName);

        BinaryRelation tmp = reachingRelation == null
                ? new BinaryRelationImpl(new ElementGroup(), var, var)
                : reachingRelation;

        mapper.getMap().put(AliasedPathImpl.empty(), tmp);

        return mapper;
    }

    public BinaryRelation getPath() {

        PathToRelationMapper<AliasedPath> mapper = createPathMapper();
        BinaryRelation result = mapper.getOverallRelation(path);

        return result;
    }

    public Collection<BinaryRelation> getPaths() {
        BinaryRelation pathRelation = getPath();
        return Collections.singleton(pathRelation);
    }

    @Override
    public Resolver resolve(P_Path0 step, String alias) {
        AliasedPath subPath = path.subPath(Maps.immutableEntry(step, alias));

        return new ResolverData(this, query, subPath, reachingRelation);
    }

    public static TernaryRelation createRelation(boolean isFwd, Var s, Var p, Var o) {
        Triple t = QueryFragment.createTriple(!isFwd, s, p, o);
        TernaryRelation result = new TernaryRelationImpl(ElementUtils.createElement(t), s, p, o);
        return result;
    }

    @Override
    public Collection<TernaryRelation> getRdfGraphSpec(boolean isFwd) {

        Element basePattern = query.getQuery().getQueryPattern();
        Var baseVar = query.getPartitionVar();
        //Var baseVar = (Var)start.asNode();
        Concept baseConcept = new Concept(basePattern, baseVar);



//		PathAccessorRdf<SimplePath> pathAccessor = new PathAccessorSimplePath();
//		PathToRelationMapper<SimplePath> mapper = new PathToRelationMapper<>(pathAccessor, "w");
//		// We need to connect relations:
//		// concept - path - triple pattern
//		BinaryRelation pathRelation = mapper.getOverallRelation(new SimplePath(steps));

        BinaryRelation pathRelation = getPath();

        BinaryRelation pathRelationWithConcept = pathRelation
            .prependOn(pathRelation.getSourceVar()).with(baseConcept)
            .toBinaryRelation();

        TernaryRelation tr = createRelation(isFwd, Vars.s, Vars.p, Vars.o);


        TernaryRelation result = tr
                .prependOn(tr.getS()).with(pathRelationWithConcept, pathRelationWithConcept.getTargetVar())
                .toTernaryRelation();

        return Arrays.asList(result);
    }

}

