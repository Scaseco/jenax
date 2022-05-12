package org.aksw.jenax.arq.util.syntax;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.commons.collections.SetUtils;
import org.aksw.commons.util.math.Lehmer;
import org.aksw.jenax.arq.util.quad.QuadPatternUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sparql.util.ExprUtils;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

/**
 * A hasher for SPARQL queries that keeps track of separate hash codes for the
 * body, the projection and the permutation of the projection.
 *
 * @author raven
 *
 */
public class QueryHash {
    protected Query query;
    protected Query bodyQuery;
    protected HashCode bodyHash;
    protected HashCode projHash;
    protected BigInteger projLehmerValue;

    public QueryHash(Query query, Query bodyQuery, HashCode bodyHash, HashCode projectionHash, BigInteger projLehmerValue) {
		super();
		this.query = query;
		this.bodyQuery = bodyQuery;
		this.bodyHash = bodyHash;
		this.projHash = projectionHash;
		this.projLehmerValue = projLehmerValue;
	}

    public Query getQuery() {
		return query;
	}

    public Query getBodyQuery() {
		return bodyQuery;
	}

    public String getBodyHashStr() {
    	return BaseEncoding.base64Url().encode(bodyHash.asBytes());
    }

    public String getProjHashStr() {
    	return BaseEncoding.base64Url().encode(projHash.asBytes());
    }

	public static QueryHash createHash(Query query) {
		HashFunction bodyHashFn = Hashing.sha256();
		HashFunction projHashFn = Hashing.murmur3_32_fixed();

    	Query bodyQuery = query.cloneQuery();
		VarExprList proj = bodyQuery.getProject();

    	if (bodyQuery.isConstructType()) {
    		Set<Var> pvars = SetUtils.asSet(PatternVars.vars(bodyQuery.getQueryPattern()));

    		Template template = bodyQuery.getConstructTemplate();
    		List<Quad> quads = template.getQuads();
    		Set<Var> vars = new TreeSet<>(QuadPatternUtils.getVarsMentioned(quads));

    		Set<Var> effProj = Sets.intersection(vars, pvars);

    		bodyQuery.setQuerySelectType();
        	proj.clear();
        	effProj.forEach(proj::add);
    	}

    	bodyQuery.setLimit(Query.NOLIMIT);
    	bodyQuery.setOffset(Query.NOLIMIT);
    	bodyQuery.setQuerySelectType();
    	bodyQuery.resetResultVars();

    	VarExprList baseProj = new VarExprList();
    	if (bodyQuery.hasGroupBy()) {
    		VarExprList vel = bodyQuery.getGroupBy();
    		baseProj.addAll(vel);
    	}

    	if (bodyQuery.hasAggregators()) {
//    		bodyQuery.getAggregators()
//
//    		List<ExprAggregator> eas = bodyQuery.getAggregators();
//    		for (ExprAggregator ea : eas) {
//    			ea.getExpr()te
//    		}
    	}

    	List<String> projStrs = new ArrayList<>();
    	for (Var var : bodyQuery.getProjectVars()) {
    		ExprVar ev = new ExprVar(var);
    		Expr expr = proj.getExpr(var);
    		Expr e = expr == null ? ev : new E_Equals(ev, expr);

    		String str = ExprUtils.fmtSPARQL(e);
    		projStrs.add(str);
    	}

    	Set<String> projSet = new TreeSet<>(projStrs);
    	Hasher projHasher = projHashFn.newHasher();
    	for(String item : projSet) {
    		projHasher.putString(item, StandardCharsets.UTF_8);
    	}
    	HashCode projHashCode = projHasher.hash();

    	bodyQuery.setQueryResultStar(true);
     	bodyQuery.resetResultVars();

    	String bodyStr = bodyQuery.toString();

    	HashCode bodyHashCode = bodyHashFn.hashString(bodyStr, StandardCharsets.UTF_8);

    	BigInteger projLehmerValue = Lehmer.lehmerValue(projStrs, Comparator.naturalOrder());

    	return new QueryHash(query, bodyQuery, bodyHashCode, projHashCode, projLehmerValue);
    }

    @Override
	public String toString() {
		return "QueryStringHashCode [query=" + query + ", bodyQuery=" + bodyQuery + ", bodyHash=" + getBodyHashStr() + ", projHash=" + getProjHashStr()
				+ ", projLehmerValue=" + projLehmerValue + "]";
	}

	public static void main(String[] args) {
		System.out.println(QueryHash.createHash(QueryFactory.create("SELECT ?s ?p { ?s ?p ?o } LIMIT 10 OFFSET 100")));

		System.out.println(QueryHash.createHash(QueryFactory.create("SELECT ?p ?s { ?s ?p ?o } LIMIT 10 OFFSET 100")));
		System.out.println(QueryHash.createHash(QueryFactory.create("SELECT ?p ?o { ?s ?p ?o } LIMIT 10")));

//        Collection<String> items = Arrays.asList("d", "c", "b", "a");
//        BigInteger value = Lehmer.lehmerValue(items, Comparator.naturalOrder());
    }

}
