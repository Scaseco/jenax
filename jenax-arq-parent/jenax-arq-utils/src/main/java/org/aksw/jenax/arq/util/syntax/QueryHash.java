package org.aksw.jenax.arq.util.syntax;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.commons.util.math.Lehmer;
import org.aksw.jenax.arq.util.node.NodeTransformLib2;
import org.aksw.jenax.arq.util.var.VarGeneratorImpl2;
import org.aksw.jenax.arq.util.var.VarUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryType;
import org.apache.jena.query.SortCondition;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.expr.aggregate.Aggregator;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sparql.util.FmtUtils;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

class LehmerHash {
    protected HashCode hash;
    protected BigInteger lehmer;

    protected LehmerHash(HashCode hash, BigInteger lehmer) {
        super();
        this.hash = hash;
        this.lehmer = lehmer;
    }

    public static LehmerHash of(HashCode hash, BigInteger lehmer) {
        return new LehmerHash(hash, lehmer);
    }

    public HashCode getHash() {
        return hash;
    }
    public BigInteger getLehmer() {
        return lehmer;
    }
}


/**
 * A hasher for SPARQL queries that keeps track of separate hash codes for the
 * body, the subset of the projection (w.r.t. visible variables), the permutation of the projection and
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
    /** The body query is essentially a version of the query with an altered projection:
     * For non-group-by queries: The projection gets replaced with SELECT *
     * For group-by-queries:
     */
//    protected Query bodyQuery;
//    protected HashCode bodyHash;
//    protected HashCode projHash;
//    protected BigInteger projLehmerValue;

    protected Query originalQuery;
    protected Query harmonizedQuery;

    protected HashCode bodyHashCode;
    protected LehmerHash aggHash;
    protected LehmerHash groupByHash;
    protected LehmerHash havingHash;
    protected LehmerHash orderByHash;
    protected LehmerHash projecHash;
    protected HashCode relabelHash;

    public QueryHash(Query originalQuery, Query harmonizedQuery, HashCode bodyHashCode, LehmerHash aggHash,
            LehmerHash groupByHash, LehmerHash havingHash, LehmerHash orderByHash, LehmerHash projecHash,
            HashCode relabelHash) {
        super();
        this.originalQuery = originalQuery;
        this.harmonizedQuery = harmonizedQuery;
        this.bodyHashCode = bodyHashCode;
        this.aggHash = aggHash;
        this.groupByHash = groupByHash;
        this.havingHash = havingHash;
        this.orderByHash = orderByHash;
        this.projecHash = projecHash;
        this.relabelHash = relabelHash;
    }

    public Query getOriginalQuery() {
        return originalQuery;
    }

    public Query getHarmonizedQuery() {
        return harmonizedQuery;
    }

    public HashCode getBodyHashCode() {
        return bodyHashCode;
    }

    public LehmerHash getAggHash() {
        return aggHash;
    }

    public LehmerHash getGroupByHash() {
        return groupByHash;
    }

    public LehmerHash getHavingHash() {
        return havingHash;
    }

    public LehmerHash getOrderByHash() {
        return orderByHash;
    }

    public LehmerHash getProjecHash() {
        return projecHash;
    }

    public HashCode getRelabelHash() {
        return relabelHash;
    }

//    public String getBodyHashStr() {
//        return BaseEncoding.base64Url().omitPadding().encode(bodyHashCode.asBytes());
//    }
//
//    public String getProjHashStr() {
//        return BaseEncoding.base64Url().omitPadding().encode(projectHash.asBytes());
//    }

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

    public static void relabelVariables(Query query) {
        Set<Var> patternVars = (Set<Var>)PatternVars.vars(new LinkedHashSet<>(), query.getQueryPattern());
        Set<Var> groupByVars = new LinkedHashSet<>();
        Set<Var> orderByVars = new LinkedHashSet<>();
        Set<Var> havingVars = new LinkedHashSet<>();
        Set<Var> projVars = new LinkedHashSet<>();
        if (query.hasGroupBy()) {
            groupByVars = VarExprListUtils.getVarsMentioned(query.getGroupBy());
            if (query.hasHaving()) {
                query.getHavingExprs().forEach(e -> ExprVars.varsMentioned(havingVars, e));
            }
        }

        if (query.hasOrderBy()) {
            ExprVars.varsMentioned(orderByVars, query.getOrderBy());
        }

        VarExprListUtils.varsMentioned(projVars, query.getProject());

        Set<Var> allVars = new LinkedHashSet<>();
        allVars.addAll(patternVars);
        allVars.addAll(groupByVars);
        allVars.addAll(orderByVars);
        allVars.addAll(havingVars);
        allVars.addAll(projVars);

        Map<Var, Var> relabel = new LinkedHashMap<>();
        Generator<Var> vargen = VarGeneratorImpl2.create("v");
        for (Var v : allVars) {
            relabel.put(v, vargen.next());
        }


        // BigInteger projLehmerValue = Lehmer.lehmerValue(projStrs, Comparator.naturalOrder());



        // NodeTransform xform = new NodeTransform
        Query result = org.aksw.jenax.util.backport.syntaxtransform.QueryTransformOps.transform(query, relabel);
//        System.out.println(result);
    }

    public static QueryHash createHash(Query query) {
        HashFunction queryPatternHashFn = Hashing.sha256();
        HashFunction aggHashFn = Hashing.murmur3_32_fixed();
        HashFunction orderByHashFn = Hashing.murmur3_32_fixed();
        HashFunction havingHashFn = Hashing.murmur3_32_fixed();
        HashFunction groupByHashFn = Hashing.murmur3_32_fixed();
        HashFunction projectHashFn = Hashing.murmur3_32_fixed();
        HashFunction varMapHashFn = Hashing.murmur3_32_fixed();

        Map<Var, Var> relabel = new LinkedHashMap<>();
        Set<Var> allVars = new LinkedHashSet<>();

        Query newQuery = new Query();

        newQuery.setQuerySelectType();

        // Query Pattern

        Element queryPattern = query.getQueryPattern();
        if (queryPattern == null) {
            queryPattern = new ElementGroup();
        }
        Set<Var> patternVars = (Set<Var>)PatternVars.vars(new LinkedHashSet<>(), queryPattern);

        Generator<Var> patternVarGen = VarGeneratorImpl2.create("v");
        patternVars.forEach(v -> getOrNext(relabel, v, patternVarGen));
        allVars.addAll(patternVars);

        NodeTransform xform = new NodeTransformSubst(relabel);

        Element newQueryPattern = ElementUtils.applyNodeTransform(queryPattern, xform);
        newQuery.setQueryPattern(newQueryPattern);

        // Aggregators

        List<ExprAggregator> newAggregators;
        if (query.hasAggregators()) {
            List<ExprAggregator> aggs = query.getAggregators();
            newAggregators = new ArrayList<>(aggs.size());
            for (ExprAggregator ea : aggs) {
                ExprAggregator newEa = transform(ea, relabel, patternVarGen, aggHashFn);
                newAggregators.add(newEa);
            }
            newQuery.getAggregators().addAll(newAggregators);
        } else {
            newAggregators = List.of();
        }

        // Group By
        Generator<Var> groupByVarGen = VarGeneratorImpl2.create("g");
        VarExprList newGroupBy = transform(query.getGroupBy(), relabel, groupByVarGen, groupByHashFn);
        newQuery.getGroupBy().addAll(newGroupBy);

        // Having
        Generator<Var> havingVarGen = VarGeneratorImpl2.create("h");
        if (query.hasHaving()) {
            List<Expr> newHavingExprs = transform(query.getHavingExprs(), relabel, havingVarGen);
            newHavingExprs.forEach(newQuery::addHavingCondition);
        }

        // Order By
        Generator<Var> orderByVarGen = VarGeneratorImpl2.create("o");
        if (query.hasOrderBy()) {
            NodeTransform xform2 = nodeTransform(relabel, orderByVarGen);
            List<SortCondition> newOrderBy = query.getOrderBy().stream()
                .map(sc -> NodeTransformLib2.transform(xform2, sc))
                .collect(Collectors.toList());
            newOrderBy.forEach(newQuery::addOrderBy);
        }

        // Projection
        Generator<Var> projectVarGen = VarGeneratorImpl2.create("p");

        QueryUtils.setQueryType(newQuery, query.queryType());

        switch (query.queryType()) {
        case SELECT:
            newQuery.setQueryResultStar(query.isQueryResultStar());
            if (!query.isQueryResultStar()) {
                VarExprList newProject = transform(query.getProject(), relabel, projectVarGen, projectHashFn);
                newQuery.getProject().addAll(newProject);
            }
            break;
        case CONSTRUCT:
            Template newTemplate = transform(query.getConstructTemplate(), relabel, projectVarGen);
            newQuery.setConstructTemplate(newTemplate);
            break;
        case ASK:
            break;
        case DESCRIBE:
            List<Node> nodes = query.getResultURIs().stream()
                .map(n -> transform(n, relabel, projectVarGen))
                .collect(Collectors.toList());
            nodes.forEach(newQuery::addDescribeNode);
            break;
        case CONSTRUCT_JSON:
            Map<String, Node> newJsonMapping = query.getJsonMapping().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, e -> transform(e.getValue(), relabel, projectVarGen)));
            newJsonMapping.forEach(newQuery::addJsonMapping);
            break;
        default:
            throw new RuntimeException("Unknown query type: " + query.queryType());
        }



        HashCode bodyHashCode = queryPatternHashFn.hashString(newQuery.getQueryPattern().toString(), StandardCharsets.UTF_8);

//        System.out.println(bodyHashCode);

        LehmerHash aggHash = hash(aggHashFn, newQuery.getAggregators(), ExprUtils::fmtSPARQL);

        LehmerHash groupByHash = toHashCode(newQuery.getGroupBy());
//        System.out.println(groupByHash);


        LehmerHash havingHash = hash(havingHashFn, newQuery.getHavingExprs(), ExprUtils::fmtSPARQL);


        LehmerHash orderByHash = hash(orderByHashFn, newQuery.getOrderBy(), QueryHash::fmt);

//        System.out.println(orderByHash);
//        System.out.println(projecHash);

        LehmerHash projectHash;
        switch (query.queryType()) {
        case SELECT:
            projectHash = toHashCode(newQuery.getProject());
            break;
        case CONSTRUCT:
            projectHash = hash(projectHashFn, newQuery.getConstructTemplate().getQuads(), FmtUtils::stringForQuad);
            break;
        case ASK:
            projectHash = LehmerHash.of(HashCode.fromInt(0), BigInteger.valueOf(0));
            break;
        case DESCRIBE:
            projectHash = hash(projectHashFn, query.getResultURIs(), NodeFmtLib::strTTL);
            break;
        case CONSTRUCT_JSON:
            projectHash = hash(projectHashFn, query.getJsonMapping().entrySet(), Objects::toString);
            break;
        default:
            throw new RuntimeException("Unknown query type: " + query.queryType());
        }


        // Var Mapping
        HashCode relabelHash = Hashing.combineUnordered(relabel.entrySet().stream()
                .map(Objects::toString)
                .map(str -> varMapHashFn.hashString(str, StandardCharsets.UTF_8))
                .collect(Collectors.toList()));

//        System.out.println(relabelHash);
//        System.out.println(newQuery);

        newQuery.setLimit(query.getLimit());
        newQuery.setOffset(query.getOffset());

        QueryHash result = new QueryHash(query, newQuery, bodyHashCode, aggHash, groupByHash, havingHash, orderByHash, projectHash, relabelHash);

//        System.out.println("Original Query:\n" + query);
//        System.out.println("Harmonized Query:\n" + newQuery);
//        System.out.println("Hash: " + result);

        return result;
//
//        Set<Var> groupByVars = new LinkedHashSet<>();
//        Set<Var> orderByVars = new LinkedHashSet<>();
//        Set<Var> havingVars = new LinkedHashSet<>();
//        Set<Var> projVars = new LinkedHashSet<>();
//
//        if (query.hasGroupBy()) {
//            groupByVars = VarExprListUtils.getVarsMentioned(query.getGroupBy());
//        }
//
//        if (query.hasHaving()) {
//            query.getHavingExprs().forEach(e -> ExprVars.varsMentioned(havingVars, e));
//        }
//
//        if (query.hasOrderBy()) {
//            ExprVars.varsMentioned(orderByVars, query.getOrderBy());
//        }
//
//        VarExprListUtils.varsMentioned(projVars, query.getProject());
//
//
//        Generator<Var> groupByVarGen = VarGeneratorImpl2.create("g");
//        Sets.difference(groupByVars, allVars).forEach(v -> relabel.put(v, groupByVarGen.next()));
//        allVars.addAll(groupByVars);
//
//        Generator<Var> orderByVarGen = VarGeneratorImpl2.create("o");
//        Sets.difference(orderByVars, allVars).forEach(v -> relabel.put(v, orderByVarGen.next()));
//        allVars.addAll(groupByVars);
//
//        for (ExprAggregator ea : query.getAggregators()) {
//            NodeTransformLib.transform(null, ea);
//        }
//
//        Generator<Var> projVarGen = VarGeneratorImpl2.create("p");
//        Sets.difference(projVars, allVars).forEach(v -> relabel.put(v, projVarGen.next()));
//        allVars.addAll(projVars);
//
//
//
//        // group by expressions (hash the exprs + lehmer)
//        VarExprList newGroupBy = VarExprListUtils.transform(query.getGroupBy(), xform);
//        Entry<HashCode, BigInteger> groupByHash = toHashCode(newGroupBy);
//        System.out.println(groupByHash);
//
//        // having
//        List<Expr> havingExprs = query.getHavingExprs();
//        // query.toString()
//
//        // order by
//        List<String> newOrderBy = query.getOrderBy().stream().map(sc -> {
//            SortCondition newSc = NodeTransformLib2.transform(xform, sc);
//            String r = fmt(newSc);
//            return r;
//        }).collect(Collectors.toList());
//
//
//        Entry<HashCode, BigInteger> orderByHash = hash(orderByHashFn, newOrderBy);
//        System.out.println(orderByHash);
//
//
//        // aggregators
//
//        // basic projection (not considering expressions)
//
//
//
//        System.out.println(newQueryPattern);
//
    }

    /** Format a sort condition */
    public static String fmt(SortCondition sc) {
        String exprStr = ExprUtils.fmtSPARQL(sc.getExpression());
        String result;
        switch (sc.getDirection()) {
        case Query.ORDER_ASCENDING:
            result = "ASC(" + exprStr + ")";
            break;
        case Query.ORDER_DESCENDING:
            result = "DESC(" + exprStr + ")";
            break;
        default:
            result = exprStr; break;
        }
        return result;
    }


    public static List<ExprAggregator> transform(List<ExprAggregator> eas, Map<Var, Var> relabel, Generator<Var> varGen, HashFunction hashFn) {
        VarExprList vel = new VarExprList();
        for (ExprAggregator ea : eas) {
            vel.add(ea.getVar(), ea);
        }
        VarExprList newVel = transform(vel, relabel, varGen, hashFn);
        List<ExprAggregator> result = VarExprListUtils.streamVarExprs(newVel)
            .map(e -> {
                ExprAggregator r = new ExprAggregator(e.getKey(), ((ExprAggregator)e.getValue()).getAggregator());
                return r;
            })
            .collect(Collectors.toList());
        return result;
    }

    public static VarExprList transform(VarExprList vel, Map<Var, Var> relabel, Generator<Var> varGen, HashFunction hashFn) {
        VarExprList result = new VarExprList();
        for (Var v : vel.getVars()) {
            Expr e = vel.getExpr(v);
            Entry<Var, Expr> contrib = transform(v, e, relabel, varGen, hashFn);
            result.add(contrib.getKey(), contrib.getValue());
        }
        return result;
    }

    // SortCondition, VarExprList

    public static List<Expr> transform(List<Expr> es, Map<Var, Var> relabel, Generator<Var> varGen) {
        return es.stream().map(e -> transform(e, relabel, varGen)).collect(Collectors.toList());
    }

    public static Expr transform(Expr e, Map<Var, Var> relabel, Generator<Var> varGen) {
        Set<Var> mentionedVars = new LinkedHashSet<>();
        ExprVars.varsMentioned(mentionedVars, e);
        for (Var mv : mentionedVars) {
            getOrNext(relabel, mv, varGen);
        }
        Expr result = NodeTransformLib.transform(new NodeTransformSubst(relabel), e);
        return result;
    }

    /** If there is an expression and the variable is not yet relabeled, then the given variable is
     *  remapped to a hash of that expression.
     *
     */
    public static Entry<Var, Expr> transform(Var v, Expr e, Map<Var, Var> relabel, Generator<Var> varGen, HashFunction hashFn) {
        // Relabel variables of the expression
        Entry<Var, Expr> result;
        if (e != null) {
            Set<Var> mentionedVars = new LinkedHashSet<>();

            ExprVars.varsMentioned(mentionedVars, e);
            for (Var mv : mentionedVars) {
                getOrNext(relabel, mv, varGen);
            }
            Expr newExpr = NodeTransformLib.transform(new NodeTransformSubst(relabel), e);
            // Hash the expression itself
            String str = ExprUtils.fmtSPARQL(newExpr);
            HashCode hashCode = hashFn.hashString(str, StandardCharsets.UTF_8);

            String newVarName = getSpecialPrefix(v) + str(hashCode);
            Var newVar = Var.alloc(newVarName);
            relabel.put(v, newVar);
            result = Map.entry(newVar, newExpr);
        } else {
            Var newVar = getOrNext(relabel, v, varGen);
            result = new SimpleEntry<>(newVar, null);
        }
        return result;
    }

    public static Var getOrNext(Map<Var, Var> relabel, Var v, Generator<Var> varGen) {
        Var result = relabel.computeIfAbsent(v, x -> nextVar(x, varGen));
        return result;
    }

    public static Var nextVar(Var baseVar, Generator<Var> varGen) {
        String prefix = getSpecialPrefix(baseVar);
        Var tmp = varGen.next();
        Var result = prefix.isEmpty() ? tmp : Var.alloc(prefix + tmp.getName());
        return result;
    }

    public static String getSpecialPrefix(Var var) {
        StringBuilder sb = new StringBuilder();
        String name = var.getName();

        // TODO Actually we should stop at the first codepoint from where all following codepoints are valid sparql var code points.
        String result = name.codePoints()
                .takeWhile(c -> !VarUtils.isValidFirstCharForVarName(c))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return result;
    }

    public static NodeTransform nodeTransform(Map<Var, Var> relabel, Generator<Var> varGen) {
        return node -> transform(node, relabel, varGen);
    }

    public static Node transform(Node node, Map<Var, Var> relabel, Generator<Var> varGen) {
        Node r = node;
        if (node.isVariable()) {
            Var v = (Var)node;
            r = getOrNext(relabel, v, varGen);
        }
        return r;
    }

    public static Template transform(Template template, Map<Var, Var> relabel, Generator<Var> varGen) {
        NodeTransform xform = node -> transform(node, relabel, varGen);
        List<Quad> quads = template.getQuads().stream()
            .map(q -> NodeTransformLib.transform(xform, q))
            .collect(Collectors.toList());
        return new Template(new QuadAcc(quads));
    }

    public static ExprAggregator transform(ExprAggregator ea, Map<Var, Var> relabel, Generator<Var> varGen, HashFunction hashFn) {
        // Relabel variables of the expression
        Set<Var> mentionedVars = new LinkedHashSet<>();

        ExprVars.varsMentioned(mentionedVars, ea.getAggregator().getExprList());
        for (Var mv : mentionedVars) {
            getOrNext(relabel, mv, varGen);
        }

        ExprList newExprList = NodeTransformLib.transform(new NodeTransformSubst(relabel), ea.getAggregator().getExprList());
        Aggregator newAgg = ea.getAggregator().copy(newExprList);
        String str = newAgg.asSparqlExpr(FmtUtils.sCxt());
        HashCode hashCode = hashFn.hashString(str, StandardCharsets.UTF_8);
        Var oldVar = ea.getVar();
        String newVarBaseName = getSpecialPrefix(oldVar) + BaseEncoding.base64Url().omitPadding().encode(hashCode.asBytes());
        Var newVar;
        for (int i = 0; ; ++i) {
            String newVarName = i == 0 ? newVarBaseName : newVarBaseName + "_" + i;
            newVar = Var.alloc(newVarName);
            if (!relabel.containsValue(newVar)) { // XXX Could index - but usually there should be no clash
                relabel.put(oldVar, newVar);
                break;
            }
        }
        ExprAggregator result = new ExprAggregator(newVar, newAgg);
        return result;
    }

    public static LehmerHash toHashCode(VarExprList vel) {
        HashFunction hashFn = Hashing.murmur3_32_fixed();
        List<String> elements = VarExprListUtils.streamVarExprs(vel).map(Object::toString).collect(Collectors.toList());
        return hash(hashFn, elements, Objects::toString);
    }

//    public static <S, T extends Comparable<T>> Entry<HashCode, BigInteger> hashIndirect(HashFunction hashFn, Collection<S> elements, Function<S, T> mapper) {
//    }

    public static <T> LehmerHash hash(HashFunction hashFn, Collection<T> elements, Function<T, String> toString) {
        List<String> strs = elements == null ? List.of() : elements.stream().map(toString::apply).collect(Collectors.toList());
        List<HashCode> hashCodes = strs.stream().map(str -> {
            HashCode r = hashFn.hashString(str, StandardCharsets.UTF_8);
            return r;
        }).collect(Collectors.toList());
        HashCode hashCode = hashCodes.isEmpty() ? HashCode.fromInt(0) : Hashing.combineUnordered(hashCodes);
        BigInteger lehmerValue = Lehmer.lehmerValue(strs, Comparator.naturalOrder());
        return LehmerHash.of(hashCode, lehmerValue);
    }

//    public static QueryHash createHashOld(Query query) {
//
//        HashFunction bodyHashFn = Hashing.sha256();
//        HashFunction projHashFn = Hashing.murmur3_32_fixed();
//
//        // Clone the query because we need a copy of the aggregator registration
//        // Query bodyQuery = query.cloneQuery();
//        Query bodyQuery = QueryTransformOps.shallowCopy(query);
//        VarExprList proj = bodyQuery.getProject();
//
//        Comparator<Var> cmp = Comparator.comparing(Object::toString);
//        if (bodyQuery.isConstructType()) {
//            // XXX Use visible vars via algebra rather than mentioned vars of the query pattern?
//            Set<Var> pvars = SetUtils.asSet(PatternVars.vars(bodyQuery.getQueryPattern()));
//
//            Template template = bodyQuery.getConstructTemplate();
//            List<Quad> quads = template.getQuads();
//            Set<Var> vars = new TreeSet<>(cmp);
//            vars.addAll(QuadPatternUtils.getVarsMentioned(quads));
//            Set<Var> effProj = Sets.intersection(vars, pvars);
//
//            bodyQuery.setQuerySelectType();
//            proj.clear();
//            effProj.forEach(proj::add);
//        }
//
//        bodyQuery.setLimit(Query.NOLIMIT);
//        bodyQuery.setOffset(Query.NOLIMIT);
//        bodyQuery.setQuerySelectType();
//
//        // VarExprList baseProj = new VarExprList(proj);
//        // Add the aggregator expressions to the projection:
//        // Queries that only differ by projection of the group by expressions
//        // only differ by the lehmer code
//        if (bodyQuery.hasGroupBy()) {
//            VarExprList vel = bodyQuery.getGroupBy();
//            proj.addAll(vel);
//        } else {
//            bodyQuery.setQueryResultStar(true);
//        }
//
//        // Ensure result variables are properly set
//        bodyQuery.resetResultVars();
//
//        // Get the visible vars as a base for computing the lehmer value
//        Op op = Algebra.compile(bodyQuery);
//        Set<Var> visibleVars = OpVars.visibleVars(op);
//
//        Set<Var> nonAggVars = new TreeSet<>(cmp);
//        Set<Var> aggVars = new TreeSet<>(cmp);
//
//        nonAggVars.addAll(getNonAggregateVars(bodyQuery));
//        aggVars.addAll(proj.getVars());
//        aggVars.removeAll(nonAggVars);
//
//        VarExprList newVel = new VarExprList();
//        for (Var var : nonAggVars) {
//            VarExprListUtils.add(newVel, var, proj.getExpr(var));
//        }
//        for (Var var : aggVars) {
//            VarExprListUtils.add(newVel, var, proj.getExpr(var));
//        }
//        proj.clear();
//        proj.addAll(newVel);
//
//        List<String> projStrs = new ArrayList<>();
//        for (Var var : bodyQuery.getProjectVars()) {
//            ExprVar ev = new ExprVar(var);
//            Expr expr = proj.getExpr(var);
//            Expr e = expr == null ? ev : new E_Equals(ev, expr);
//
//            String str = ExprUtils.fmtSPARQL(e);
//            projStrs.add(str);
//        }
//
//        Set<String> projSet = new TreeSet<>(projStrs);
//        Hasher projHasher = projHashFn.newHasher();
//        for(String item : projSet) {
//            projHasher.putString(item, StandardCharsets.UTF_8);
//        }
//        HashCode projHashCode = projHasher.hash();
//
//        bodyQuery.resetResultVars();
//
//        String bodyStr = bodyQuery.toString();
//
//        HashCode bodyHashCode = bodyHashFn.hashString(bodyStr, StandardCharsets.UTF_8);
//
//        BigInteger projLehmerValue = Lehmer.lehmerValue(projStrs, Comparator.naturalOrder());
//
//        return new QueryHash(query, bodyQuery, bodyHashCode, projHashCode, projLehmerValue);
//    }

    public static String str(HashCode hashCode) {
        return str(hashCode.asBytes());
    }

    /** Return a copy of the byte array with any leading 0s removed */
    public static byte[] trim(byte[] rawBytes) {
        int n = rawBytes.length;
        int i = 0;
        for (i = 0; i < n; ++i) {
            if (rawBytes[i] != 0) {
                break;
            }
        }
        byte[] result = Arrays.copyOfRange(rawBytes, i, n);
        return result;
    }

    /**
     * Create a base64url encoded string from the trimmed byte array.
     * If the trimmed byte array is empty, then a single 0 byte is used instead.
     */
    public static String str(byte[] rawBytes) {
        byte[] bytes = trim(rawBytes);
        if (bytes.length == 0) {
            bytes = new byte[] { 0 };
        }
        BaseEncoding encoding = BaseEncoding.base64Url().omitPadding();
        String result = encoding.encode(bytes);
        return result;
    }

    public String str(BigInteger value) {
        return str(value.toByteArray());
    }

    protected String getQueryTypePrefix(QueryType queryType) {
        // (a)sk, (c)onstruct, (d)escribe, (j)son, (s)elect
        return Character.toString(queryType.name().charAt(0)).toLowerCase();
    }

    @Override
    public String toString() {
        Query query = getHarmonizedQuery();
        String sliceHash = query.hasOffset() ? "" + query.getOffset() : "";
        sliceHash += query.hasLimit() ? "+" + query.getLimit() : "";

        String baseHash =
            str(getBodyHashCode()) + "/" +
            str(getGroupByHash().getHash()) + "/" +
            str(getHavingHash().getHash()) + "/" +
            getQueryTypePrefix(harmonizedQuery.queryType()) + "/" +
            str(getProjecHash().getHash()) + "/" +
            str(getOrderByHash().getHash()) + "/" +
            str(getGroupByHash().getLehmer()) + "/" +
            str(getHavingHash().getLehmer()) + "/" +
            // Omit projection hash on ask queries
            (!harmonizedQuery.isAskType()
                    ? str(getProjecHash().getLehmer()) + "/" +
                        str(getOrderByHash().getLehmer()) + "/"
                    : "") +
            str(getRelabelHash())
            ;

        return baseHash + (sliceHash.isEmpty() ? "" : "/" + sliceHash);

        // return "QueryStringHashCode [Original Query:\n" + query + "BodyQuery:\n" + bodyQuery + ", bodyHash=" + getBodyHashStr() + ", projHash=" + getProjHashStr()
        //        + ", projLehmerValue=" + projLehmerValue + "]";
    }

    public static void main(String[] args) {
//		System.out.println(QueryHash.createHash(QueryFactory.create("SELECT ?s ?p { ?s ?p ?o } LIMIT 10 OFFSET 100")));
//
//		System.out.println(QueryHash.createHash(QueryFactory.create("SELECT ?p ?s { ?s ?p ?o } LIMIT 10 OFFSET 100")));
//		System.out.println(QueryHash.createHash(QueryFactory.create("SELECT ?p ?o { ?s ?p ?o } LIMIT 10")));

        System.out.println(QueryHash.createHash(QueryFactory.create("SELECT ?s COUNT(?p) { ?s ?p ?o } GROUP BY ?s STR(?o) ORDER BY DESC(?s) DESC(STR(?o)) LIMIT 10 OFFSET 2")));
        System.out.println(QueryHash.createHash(QueryFactory.create("SELECT COUNT(?y) ?x { ?x ?y ?z } GROUP BY ?x STR(?z) ORDER BY DESC(STR(?z)) DESC(?x) LIMIT 10 OFFSET 2")));
        // System.out.println(QueryHash.createHash(QueryFactory.create("SELECT (?x AS ?y) { ?s ?p ?o } LIMIT 10 OFFSET 2")));

//        Collection<String> items = Arrays.asList("d", "c", "b", "a");
//        BigInteger value = Lehmer.lehmerValue(items, Comparator.naturalOrder());
    }

}
