package org.aksw.jenax.arq.util.syntax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.jenax.arq.util.expr.ExprListUtils;
import org.aksw.jenax.arq.util.var.VarGeneratorBlacklist;
import org.aksw.jenax.util.backport.syntaxtransform.ElementTransformer;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.E_Regex;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.expr.ExprTransformSubstitute;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementVisitor;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCopyBase;

/**
 * Rewrites a query's dataset description so that it becomes part of the query pattern.
 *
 * @author raven
 *
 */
public class ElementTransformDatasetDescription
    extends ElementTransformCopyBase
{
    protected ExprList defaultGraphExprs;
    protected ExprList namedGraphExprs;
    protected Stack<Node> graphs;

    protected Generator<Var> varGen;

    public ElementTransformDatasetDescription(Stack<Node> graphs, Generator<Var> varGen, ExprList defaultGraphExprs, ExprList namedGraphExprs) {
        this.graphs = graphs;
        this.varGen = varGen;
        this.defaultGraphExprs = defaultGraphExprs;
        this.namedGraphExprs = namedGraphExprs;
    }

    public static ElementTransformDatasetDescription create(Stack<Node> graphs, Element e, DatasetDescription dd) {
        Collection<Var> vars = PatternVars.vars(e);
        Generator<Var> varGen = VarGeneratorBlacklist.create("__dg_", vars);

        ExprList defaultGraphExprs = ExprListUtils.fromUris(dd.getDefaultGraphURIs());
        ExprList namedGraphExprs = ExprListUtils.fromUris(dd.getNamedGraphURIs());

//        namedGraphExprs.add(NodeValue.makeNode(NodeFactory.createURI("ng1")));
//        namedGraphExprs.add(new E_Regex(new E_Str(new ExprVar("x")), NodeValue.makeString(".*cng.*"), null));
//        namedGraphExprs.add(NodeValue.makeNode(NodeFactory.createURI("ng2")));

        ElementTransformDatasetDescription result = new ElementTransformDatasetDescription(graphs, varGen, defaultGraphExprs, namedGraphExprs);
        return result;
    }

    @Override
    public Element transform(ElementTriplesBlock el) {
        Element result = applyDefaultGraphs(el);
        return result;
    }

    @Override
    public Element transform(ElementPathBlock el) {
        Element result = applyDefaultGraphs(el);
        return result;
    }

    public Element applyDefaultGraphs(Element el) {
        Element result = null;

        // If there are no graphs, inject a graph block constrained to
        // the default graphs
        if(graphs.isEmpty() && !defaultGraphExprs.isEmpty()) {
            if (defaultGraphExprs.size() == 1) {
                Expr expr = defaultGraphExprs.iterator().next();
                if (expr.isConstant()) {
                    result = applyGraphs(varGen, expr.getConstant().asNode(), el, ExprList.emptyList);
                }
            }

            if (result == null) {
                Var v = varGen.next();
                result = applyGraphs(varGen, v, el, defaultGraphExprs);
            }
        } else {
            result = el;
        }

        return result;
    }


    /**
     * In the simplest case this method creates the expression "base IN (exprs)".
     * However, expressions may make use of up to one variable such as REGEX(?x, 'foobar').
     * That variable can be subsituted by the "base" expression.
     * Hence, in general the returned expression is a disjunction comprising "IN" elements
     * (for constants and variable free function calls)
     * and substituted expressions (for single-variable expressions).
     *
     * The semantics of expressions differ dependending on the number of variables:
     * <ul>
     *   <li>0: Effective constants that are matched against the base expressions by value.</li>
     *   <li>1: Boolean expressions that are evaluated by substituting their variable with the base expression.</li>
     * </ul>
     */
    public static Expr buildFilterExpr(Expr base, ExprList conditions) {
        Expr result = null;
        Expr disjunction = null;
        ExprList oneOfBatch = new ExprList();
        List<Expr> exprs = conditions.getList();
        int n = exprs.size();
        boolean falseSeen = false;
        for (int i = 0; i < n; ++i) {
            Expr expr = exprs.get(i);

            NodeValue constant = null;
            if (!expr.isConstant()) {
                Set<Var> vars = expr.getVarsMentioned();
                int vs = vars.size();
                if (vs <= 1) {
                    if (!oneOfBatch.isEmpty()) {
                        Expr contrib = oneOfBatch.size() == 1
                                ? new E_Equals(base, oneOfBatch.get(0))
                                : new E_OneOf(base, oneOfBatch);
                        disjunction = disjunction == null ? contrib : new E_LogicalOr(disjunction, contrib);
                        oneOfBatch = new ExprList();
                    }
                    Var var = vs == 1 ? vars.iterator().next() : null;

                    if (base.isConstant()) {
                        Binding b = var == null
                                ? BindingFactory.empty()
                                : BindingFactory.binding(var, base.getConstant().asNode());

                        // Evaluation may fail if custom functions are involved - in that case
                        // just fall-through to substition
                        Expr evaled = ExprLib.evalOrNull(expr, b, null);

                        if (evaled != null) {
                            if (var == null) {
                                constant = evaled.getConstant();
                            } else {
                                // condition semantics
                                NodeValue value = evaled.getConstant();
                                boolean isTrue = XSDFuncOp.booleanEffectiveValue(value);
                                if (isTrue) {
                                    result = NodeValue.TRUE;
                                    break;
                                } else {
                                    falseSeen = true;
                                }
                            }
                        }
                    }

                    if (constant == null) {
                        Expr contrib = ExprTransformer.transform(new ExprTransformSubstitute(var, base), expr);
                        disjunction = disjunction == null ? contrib : new E_LogicalOr(disjunction, contrib);
                    }
                } else {
                    throw new IllegalStateException("Expressions may only make use of at most one variables - encountered: " + expr);
                }
            } else {
                constant = expr.getConstant();
            }

            if (constant != null) {
                if (base.isConstant()) {
                    if (base.equals(constant)) {
                        result = NodeValue.TRUE;
                        break;
                    } else {
                        falseSeen = true;
                    }
                } else {
                    oneOfBatch.add(constant);
                }
            }
        }

        if (result == null) {
            if (!oneOfBatch.isEmpty()) {
                Expr contrib = oneOfBatch.size() == 1
                        ? new E_Equals(base, oneOfBatch.get(0))
                        : new E_OneOf(base, oneOfBatch);
                disjunction = disjunction == null ? contrib : new E_LogicalOr(disjunction, contrib);
            }

            if (disjunction == null) {
                result = NodeValue.booleanReturn(!falseSeen);
            } else {
                result = disjunction;
            }
        }

        return result;
    }

    public static Element applyGraphs(Generator<Var> varGen, Node gn, Element elt1, ExprList exprs) {
        //System.out.println("apply " + gn);
        Element result;

        if (!exprs.isEmpty()) {
            Node gv;
            Expr ge;
            ExprList tmp;
            if(gn.isURI() || gn.isLiteral()) {
                gv = gn;
                ge = NodeValue.makeNode(gn);
                tmp = exprs;
            } else if(gn.isVariable()) {
                gv = (Var)gn;
                ge = new ExprVar(gv);
                tmp = exprs;
            } else if(gn.isBlank()) {
                gv = varGen.next();
                ge = new ExprVar(gv);
                tmp = exprs;
            } else {
                // Could be an RDFStar triple
                throw new RuntimeException("Unexpected case");
            }

            //ExprVar ev = new ExprVar(v);

            Element el = new ElementNamedGraph(gv, elt1);
            Expr filterExpr = buildFilterExpr(ge, tmp);
            if (NodeValue.TRUE.equals(filterExpr)) {
                result = el;
            } else {
                ElementFilter filter = new ElementFilter(filterExpr);

                ElementGroup group = new ElementGroup();
                group.addElement(el);
                group.addElement(filter);

                result = group;
            }
        } else {
            result = new ElementNamedGraph(gn, elt1);
        }

        return result;
    }

    @Override
    public Element transform(ElementNamedGraph el, Node gn, Element elt1) {
        // TODO If gn is concrete then we can filter away all constant namedGraphExprs that differ
        // If gn is a variable and nameGraphExprs is just one concrete node we can just substitute it
        Element result = applyGraphs(varGen, gn, elt1, namedGraphExprs);
        return result;
    }

    public static Query rewrite(Query query) {
        DatasetDescription dd = query.getDatasetDescription();
        Query result;
        if(dd != null) {
            result = query.cloneQuery();
            boolean usesResultStar = result.isQueryResultStar();
            List<Var> beforeRewriteVars = null;
            if (usesResultStar) {
                beforeRewriteVars = new ArrayList<>(result.getProjectVars());
            }

            Element before = result.getQueryPattern();
            Element after = rewrite(before, dd);
            result.setQueryPattern(after);
            result.getGraphURIs().clear();
            result.getNamedGraphURIs().clear();

            if (usesResultStar) {
                // The following line is needed to clear the cache of
                // previously computed project vars
                result.setQueryResultStar(true);

                List<Var> afterRewriteVars = result.getProjectVars();
                if (!beforeRewriteVars.equals(afterRewriteVars)) {
                    result.setQueryResultStar(false);
                    result.getProject().clear();
                    result.addProjectVars(beforeRewriteVars);
                }
            }


        } else {
            result = query;
        }

        return result;
    }

    public static Element rewrite(Element element, DatasetDescription dd) {
        final Stack<Node> graphs = new Stack<>();

        ExprTransform exprTransform = new ExprTransformCopy();
        ElementTransform elementTransform = ElementTransformDatasetDescription.create(graphs, element, dd);
        ElementVisitor beforeVisitor = new ElementVisitorBase() {
            @Override
            public void visit(ElementNamedGraph el) {
                graphs.push(el.getGraphNameNode());
                //System.out.println("push " + el.getGraphNameNode());
            }
        };
        ElementVisitor afterVisitor = new ElementVisitorBase() {
            @Override
            public void visit(ElementNamedGraph el) {
                graphs.pop();
                //System.out.println("pop " + el.getGraphNameNode());
            }
        };

        Element result = ElementTransformer.transform(element, elementTransform, exprTransform, beforeVisitor, afterVisitor);

        return result;
    }

    public static void main(String[] args) {
        //Query query = QueryFactory.create("SELECT * { { ?s ?p ?o } Union { Graph ?g { ?s ?p ?o } } }");
        //Query query = QueryFactory.create("SELECT * { { { Select * { ?s ?p ?o . Filter(?p = <p>) } } } Union { Graph ?g { ?s ?p ?o } } }");
        Query query = QueryFactory.create("SELECT * { { ?s ?p ?o . Graph ?x { ?a ?b ?c } } Union { Graph <urn:example:cng> { ?s ?p ?o } } }");
        query.addGraphURI("dg1");
        query.addGraphURI("dg2");
        query.addNamedGraphURI("ng1");
        query.addNamedGraphURI("ng2");
        // query.addNamedGraphURI("urn:example:cng");

        Query tmp = rewrite(query);

        Op op = Algebra.compile(tmp);
        //Op op2 = Transformer.transformSkipService(new TransformFilterPlacement(), op) ;
        tmp = OpAsQuery.asQuery(op);

        System.out.println(tmp);
    }
}
