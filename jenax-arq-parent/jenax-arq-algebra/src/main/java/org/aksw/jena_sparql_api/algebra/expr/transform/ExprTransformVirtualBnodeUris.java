package org.aksw.jena_sparql_api.algebra.expr.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.util.function.FixpointIteration;
import org.aksw.jena_sparql_api.algebra.transform.TransformExprToBasicPattern;
import org.aksw.jena_sparql_api.algebra.transform.TransformPullFiltersIfCanMergeBGPs;
import org.aksw.jena_sparql_api.algebra.transform.TransformReplaceConstants;
import org.aksw.jena_sparql_api.user_defined_function.UserDefinedFunctions;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.arq.util.var.VarGeneratorBlacklist;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.stmt.core.SparqlStmtMgr;
import com.google.common.collect.Maps;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.optimize.TransformMergeBGPs;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_NotEquals;
import org.apache.jena.sparql.expr.E_NotOneOf;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.E_OneOfBase;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprFunction2;
import org.apache.jena.sparql.expr.ExprFunctionN;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.expr.ExprTransformSubstitute;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.user.UserDefinedFunctionDefinition;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.util.ExprUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decode "blanknode URIs" - i.e. URIs that represent blank nodes, such as bnode://{blank-node-label}
 * This means
 * ?x = <bnode://foobar>
 * becomes
 * encode(?x) = decode(<bnode://foobar>)
 * encode(?x) = 'foobar'
 *
 *
 * Issue: How to deal with queries that test for blank nodes?
 * In principle, this rewrite virtually eleminates them, so maybe the most reasonable approach is
 * to simply rewrite isBlank to true.
 * in fact, the standard bnode function would also have to yield bnode uris instead
 *
 *
 * bnodeLabel(?x) := if ?x is a blank node, return its label as an xsd:string, type error otherwise
 *
 *
 * Auxiliary functions (defined in terms of the aforementioned and SPARQL standard functions)
 *
 * typeError() := abs("")
 * encodeBnodeUri(xsd:string ?x) := URI(CONCAT('bnode://', ?x))   create an URI from a blank node label
 * isBnodeUri(uri) := isURI(?x) && STRSTARTS(STR(?x), 'bnode://') true if uri represents a bnode
 * decodeBnodeUri(uri ?x) := IF(isBnodeURI(?x), STRAFTER(STR(?x), 'bnode://'), typeError()), extract the blank node label from a URI
 * forceBnodeUri(?x) -> IF(isBlank(?x), encodeBnodeURI(bnodeLabel(?x)), ?x)
 *
 *
 *
 * Transformations:
 * - Given ?x = const
 *
 * if(isBnodeURI(const)) {
 *   emit bnodeLabel(?x) = decodeBnodeURI(uri)
 * }
 *
 * @author raven
 *
 */
public class ExprTransformVirtualBnodeUris
    extends ExprTransformCopy
{
    public static final String ns = "http://ns.aksw.org/function/";

    // These function IRIs must be provided as macros
    public static final String bidOfFnIri = ns + "bidOf";
    public static final String decodeBnodeIriFnIri = ns + "decodeBnodeIri";
    public static final String isBnodeIriFnIri = ns + "isBnodeIri";
    public static final String forceBnodeIriFnIri = ns + "forceBnodeIri";

    protected Map<String, UserDefinedFunctionDefinition> macros;
    protected Map<String, Boolean> propertyFunctions;


    public ExprTransformVirtualBnodeUris(
            Map<String, UserDefinedFunctionDefinition> macros,
            Map<String, Boolean> propertyFunctions) {
        super();
        this.macros = macros;
        this.propertyFunctions = propertyFunctions;
    }

    @Override
    public Expr transform(ExprFunctionN func, ExprList args) {
        Expr result = null;

        if (func instanceof E_OneOfBase) {
            E_OneOfBase e = (E_OneOfBase)func;
            Expr lhs = e.getLHS();
            ExprList rhs = e.getRHS();

            List<Expr> bnodeConsts = rhs.getList().stream().filter(x -> x.isConstant() && isBnodeIri(x.getConstant())).collect(Collectors.toList());
            if (!bnodeConsts.isEmpty()) {
                List<Expr> exprs = new ArrayList<>();
                List<Expr> nonBnodes = rhs.getList().stream().filter(x -> !bnodeConsts.contains(x)).collect(Collectors.toList());

                if (func instanceof E_OneOf) {
                    if (!nonBnodes.isEmpty()) {
                        exprs.add(new E_OneOf(lhs, new ExprList(nonBnodes)));
                    }
                    for (Expr bnodeConst : bnodeConsts) {
                        E_Equals rawEq = new E_Equals(lhs, bnodeConst);
                        Expr eq = transform(rawEq, rawEq.getArg1(), rawEq.getArg2());
                        exprs.add(eq);
                    }
                    result = org.aksw.jenax.arq.util.expr.ExprUtils.orifyBalanced(exprs);
                } else if (func instanceof E_NotOneOf) {
                    if (!nonBnodes.isEmpty()) {
                        exprs.add(new E_NotOneOf(lhs, new ExprList(nonBnodes)));
                    }
                    for (Expr bnodeConst : bnodeConsts) {
                        E_NotEquals rawEq = new E_NotEquals(lhs, bnodeConst);
                        Expr eq = transform(rawEq, rawEq.getArg1(), rawEq.getArg2());
                        exprs.add(eq);
                    }
                    result = org.aksw.jenax.arq.util.expr.ExprUtils.andifyBalanced(exprs);
                } else {
                    throw new IllegalStateException("Should never come here");
                }
            }
        }

        if (result == null) {
            result = func.copy(args);
        }

        return result;
    }

    @Override
    public Expr transform(ExprFunction2 func, Expr a, Expr b) {

        ExprFunction2 result = null;

        if(!a.isConstant() && b.isConstant()) {
            result = trySubst(func, a, b, false);
            a = result.getArg1();
            b = result.getArg2();
        }

        if(a.isConstant() && !b.isConstant()) {
            result = trySubst(func, b, a, true);
        }

        if(result == null) {
            result = (ExprFunction2)super.transform(func, a, b);
        }

        return result;
    }

    public static <T extends ExprFunction2> T copy(T func, Expr a, Expr b, boolean swapped) {
        @SuppressWarnings("unchecked")
        T result = swapped ? (T)func.copy(b, a) : (T)func.copy(a, b);
        return result;
    }

    public static Node bnodeToIri(Node node) {
        Node result = node.isBlank()
                ? NodeFactory.createURI("bnode://" + node.getBlankNodeId().getLabelString())
                : node;
        return result;
    }

//    public Node forceBnodeIri(Node node) {
//        vel.add(v, expandMacro(macros, forceBnodeIriFnIri, new ExprVar(map.get(v))));
//    }

    public boolean isBnodeIri(NodeValue in) {
        // NodeValue in = NodeValue.makeNode(node);
        NodeValue out = UserDefinedFunctions.eval(macros, isBnodeIriFnIri, in);
        boolean result = out.getBoolean();
        return result;
    }

    public NodeValue decodeBnodeIriFn(NodeValue in) {
        NodeValue bnodeLabel;
        try {
            bnodeLabel = UserDefinedFunctions.eval(macros, decodeBnodeIriFnIri, in);
        } catch(ExprEvalException e) {
            // FIXME We should induce a type error here
            bnodeLabel = NodeValue.FALSE;
        }
        return bnodeLabel;
    }

    // x = <bnode://foo> --> bidOf(?x) = decodeBnodeIri(<bnode://foo>)
    public ExprFunction2 trySubst(ExprFunction2 func, Expr lhs, Expr b, boolean swapped) {
        NodeValue rhs = b.getConstant();

        boolean isRhsBnodeUri = isBnodeIri(rhs); // eval(macros, isBnodeIriFnIri, rhs).getBoolean();
        ExprFunction2 result;
        if(isRhsBnodeUri) {
            NodeValue bnodeLabel = decodeBnodeIriFn(rhs);
//            try {
//                bnodeLabel = eval(macros, decodeBnodeIriFnIri, rhs);
//            } catch(ExprEvalException e) {
//                // FIXME We should induce a type error here
//                bnodeLabel = NodeValue.FALSE;
//            }

            Expr x = macros.get(bidOfFnIri).getBaseExpr();


            Expr labelCondition = ExprTransformer.transform(new ExprTransformSubstitute(Vars.x, lhs), x);
            result = copy(func, labelCondition, bnodeLabel, swapped);
        } else {
            result = copy(func, lhs, b, swapped);
        }

        return result;
    }

    private static final Logger logger = LoggerFactory.getLogger(ExprTransformVirtualBnodeUris.class);

    public Query rewrite(Query query) {
        Query result = QueryUtils.rewrite(query, op -> {
            Op a = TransformReplaceConstants.transform(op, x -> x.isURI() ? UserDefinedFunctions.eval(macros, isBnodeIriFnIri, NodeValue.makeNode(x)).getBoolean() : false);
            // new ExprTransformVirtualBnodeUris()
            Op b = Transformer.transform(null, this, a);
            Op c = forceBnodeUris(b);//ExprTransformVirtualBnodeUris.forceBnodeUris(b);

            Op d = TransformExprToBasicPattern.transform(c, fn -> {
                String id = org.aksw.jenax.arq.util.expr.ExprUtils.getFunctionId(fn.getFunction());
                Boolean subjectAsOutput = propertyFunctions.get(id);
                Entry<String, Boolean> r = subjectAsOutput == null ? null : Maps.immutableEntry(id, subjectAsOutput);
//                //System.out.println(id);
//                if("str".equals(id)) {
//                    return Maps.immutableEntry("http://foo.bar/baz", false);
//                }
                return r;
            });

            Op e = FixpointIteration.apply(d, x -> {
                x = TransformPullFiltersIfCanMergeBGPs.transform(x);
                x = Transformer.transform(new TransformMergeBGPs(), x);
                // TODO Add a transformation that tidies up
                // sequences of OpProject and OpExten
//        		x = Transformer.transform(new TransformPro(), x);
                return x;
            });


            return e;
        });

        //System.out.println("Rewrote query\n" + query + " to\n" + result);
        logger.debug("Rewrote query\n" + query + " to\n" + result);
        return result;
    }

    public static ExprTransformVirtualBnodeUris createTransformFromUdfModel(Model model, Collection<String> activeProfiles) {
        Set<String> profiles = new HashSet<>(activeProfiles);
        Map<String, UserDefinedFunctionDefinition> map = UserDefinedFunctions.load(model, profiles);

        // FIXME Load property functions from model
        Map<String, Boolean> propertyFunctions = Collections.singletonMap("http://www.ontotext.com/owlim/entity#id", false);
        ExprTransformVirtualBnodeUris result = new ExprTransformVirtualBnodeUris(map, propertyFunctions);

        return result;
    }

    public Op forceBnodeUris(Op op) {
        List<Var> visibleVars = new ArrayList<>(OpVars.visibleVars(op));
        Set<Var> forbiddenVars = new HashSet<>(OpVars.mentionedVars(op));

        // Rename all visible vars
        Map<Var, Var> map = visibleVars.stream()
                .collect(Collectors.toMap(
                        v -> v, v -> VarGeneratorBlacklist.create(v.getName(), forbiddenVars).next()));

        Op tmp = NodeTransformLib.transform(n -> n.isVariable() ? map.getOrDefault(n, (Var)n) : n, op);

        VarExprList vel = new VarExprList();
        for(Var v : visibleVars) {
            vel.add(v, UserDefinedFunctions.expandMacro(macros, forceBnodeIriFnIri, new ExprVar(map.get(v))));
        }
        Op result = new OpProject(OpExtend.create(tmp, vel), visibleVars);


        return result;
    }


    public static void main(String[] args) {
        // Expr input = ExprUtils.parse("?x = <bnode://foobar>");
        Expr input = ExprUtils.parse("?x NOT IN (<bnode://123>, <urn:foo>, <bnode://456>, <urn:bar>)");
//		Expr input = ExprUtils.parse("<bnode://foo> = <bnode://bar>");

        Model model = RDFDataMgr.loadModel("bnode-rewrites.ttl");
        SparqlStmtMgr.execSparql(model, "udf-inferences.sparql");

//        Set<String> profiles = new HashSet<>(Arrays.asList("http://ns.aksw.org/profile/jena"));
      Set<String> profiles = new HashSet<>(Arrays.asList("http://ns.aksw.org/profile/graphdb"));
        ExprTransformVirtualBnodeUris xform = createTransformFromUdfModel(model, profiles);

        Expr output = ExprTransformer.transform(xform, input);
        System.out.println(output);

//        Map<String, UserDefinedFunctionDefinition> map = UserDefinedFunctions.load(model, profiles);
//
//        // FIXME Load property functions from model
//        Map<String, Boolean> propertyFunctions = Collections.singletonMap("http://www.ontotext.com/owlim/entity#id", false);
//
//
//        ExprTransformVirtualBnodeUris xform = new ExprTransformVirtualBnodeUris(map, propertyFunctions);
        //Expr actual = ExprTransformer.transform(new ExprTransformBnodeDecode(), input);
        //System.out.println(actual);

//		Query query = QueryFactory.create("SELECT * { ?s a ?t . ?s ?p ?o }");
//		Query query = QueryFactory.create("CONSTRUCT { ?s ?p ?o } { ?s <bnode://foo> ?t . ?s ?p ?o . FILTER(?p = <bnode://bar>)}");
        Query query = QueryFactory.create("CONSTRUCT { ?s ?p ?o } { ?s <bnode://foo> ?t . ?s ?p ?o . FILTER(?p = <bnode://bar>)} ORDER BY ?s");
        Query actual = xform.rewrite(query);

        //		Op op = Algebra.compile(query);
//		op = forceBnodeUris(op);
//		Query actual = OpAsQuery.asQuery(op);
        System.out.println(actual);

    }
}
