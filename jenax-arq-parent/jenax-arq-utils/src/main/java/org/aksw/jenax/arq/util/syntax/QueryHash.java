package org.aksw.jenax.arq.util.syntax;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.commons.collections.SetUtils;
import org.aksw.commons.util.math.Lehmer;
import org.aksw.jenax.arq.util.quad.QuadPatternUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.apache.jena.sparql.util.ExprUtils;

import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

/**
 * A hasher for SPARQL queries that keeps track of separate hash codes for the
 * body, the projection and the permutation of the projection.
 *
 * The hash for
 * <pre>
 * SELECT COUNT(?p) { ?s ?p ?o } GROUP BY STR(?o) LIMIT 10 OFFSET 2
 * </pre>
 * is
 * <pre>
 * ftiGBh8SJSZ89mbO9FOsCtHSSD1t3nqqTox3JNisfvI/MD9wyw/1/2+10
 * </pre>
 *
 * @author raven
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
        return BaseEncoding.base64Url().omitPadding().encode(bodyHash.asBytes());
    }

    public String getProjHashStr() {
        return BaseEncoding.base64Url().omitPadding().encode(projHash.asBytes());
    }

    /** Return the set of result variables that DO NOT map to an expression making use of an aggregator */
    public static Set<Var> getNonAggregateVars(Query query) {
        Set<Var> result = new HashSet<>();
        VarExprList proj = query.getProject();

        for (Var var : proj.getVars()) {
            Expr expr = proj.getExpr(var);

            boolean containsExprAggregator = expr == null
                    ? false
                    : org.aksw.jenax.arq.util.expr.ExprUtils.containsExprAggregator(expr);

            if (!containsExprAggregator) {
                result.add(var);
            }
        }

        return result;
    }

    public static QueryHash createHash(Query query) {
        HashFunction bodyHashFn = Hashing.sha256();
        HashFunction projHashFn = Hashing.murmur3_32_fixed();

        // Clone the query because we need a copy of the aggregator registration
        // Query bodyQuery = query.cloneQuery();
        Query bodyQuery = QueryTransformOps.shallowCopy(query);
        VarExprList proj = bodyQuery.getProject();

        Comparator<Var> cmp = Comparator.comparing(Object::toString);
        if (bodyQuery.isConstructType()) {
            // XXX Use visible vars via algebra rather than mentioned vars of the query pattern?
            Set<Var> pvars = SetUtils.asSet(PatternVars.vars(bodyQuery.getQueryPattern()));

            Template template = bodyQuery.getConstructTemplate();
            List<Quad> quads = template.getQuads();
            Set<Var> vars = new TreeSet<>(cmp);
            vars.addAll(QuadPatternUtils.getVarsMentioned(quads));
            Set<Var> effProj = Sets.intersection(vars, pvars);

            bodyQuery.setQuerySelectType();
            proj.clear();
            effProj.forEach(proj::add);
        }

        bodyQuery.setLimit(Query.NOLIMIT);
        bodyQuery.setOffset(Query.NOLIMIT);
        bodyQuery.setQuerySelectType();

        // VarExprList baseProj = new VarExprList(proj);
        // Add the aggregator expressions to the projection:
        // Queries that only differ by projection of the group by expressions
        // only differ by the lehmer code
        if (bodyQuery.hasGroupBy()) {
            VarExprList vel = bodyQuery.getGroupBy();
            proj.addAll(vel);
        } else {
            bodyQuery.setQueryResultStar(true);
        }

        // Ensure result variables are properly set
        bodyQuery.resetResultVars();

        // Get the visible vars as a base for computing the lehmer value
        Op op = Algebra.compile(bodyQuery);
        Set<Var> visibleVars = OpVars.visibleVars(op);

        Set<Var> nonAggVars = new TreeSet<>(cmp);
        Set<Var> aggVars = new TreeSet<>(cmp);

        nonAggVars.addAll(getNonAggregateVars(bodyQuery));
        aggVars.addAll(proj.getVars());
        aggVars.removeAll(nonAggVars);

        VarExprList newVel = new VarExprList();
        for (Var var : nonAggVars) {
            VarExprListUtils.add(newVel, var, proj.getExpr(var));
        }
        for (Var var : aggVars) {
            VarExprListUtils.add(newVel, var, proj.getExpr(var));
        }
        proj.clear();
        proj.addAll(newVel);

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

        bodyQuery.resetResultVars();

        String bodyStr = bodyQuery.toString();

        HashCode bodyHashCode = bodyHashFn.hashString(bodyStr, StandardCharsets.UTF_8);

        BigInteger projLehmerValue = Lehmer.lehmerValue(projStrs, Comparator.naturalOrder());

        return new QueryHash(query, bodyQuery, bodyHashCode, projHashCode, projLehmerValue);
    }

    @Override
    public String toString() {
        String baseHash = getBodyHashStr() + "/" + getProjHashStr() + "/" + projLehmerValue;
        String sliceHash = query.hasOffset() ? "" + query.getOffset() : "";
        sliceHash += query.hasLimit() ? "+" + query.getLimit() : "";

        return baseHash + (sliceHash.isEmpty() ? "" : "/" + sliceHash);

        // return "QueryStringHashCode [Original Query:\n" + query + "BodyQuery:\n" + bodyQuery + ", bodyHash=" + getBodyHashStr() + ", projHash=" + getProjHashStr()
        //        + ", projLehmerValue=" + projLehmerValue + "]";
    }

    public static void main(String[] args) {
//		System.out.println(QueryHash.createHash(QueryFactory.create("SELECT ?s ?p { ?s ?p ?o } LIMIT 10 OFFSET 100")));
//
//		System.out.println(QueryHash.createHash(QueryFactory.create("SELECT ?p ?s { ?s ?p ?o } LIMIT 10 OFFSET 100")));
//		System.out.println(QueryHash.createHash(QueryFactory.create("SELECT ?p ?o { ?s ?p ?o } LIMIT 10")));

        System.out.println(QueryHash.createHash(QueryFactory.create("SELECT COUNT(?p) { ?s ?p ?o } GROUP BY STR(?o) LIMIT 10 OFFSET 2")));

//        Collection<String> items = Arrays.asList("d", "c", "b", "a");
//        BigInteger value = Lehmer.lehmerValue(items, Comparator.naturalOrder());
    }

}
