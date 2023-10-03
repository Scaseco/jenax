package org.aksw.jenax.arq.util.triple;

import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aksw.jenax.arq.util.exec.ExecutionContextUtils;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.jenax.arq.util.node.NodeTransformLib2;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;


/**
 * A single triple pattern combined with a filter and a direction.
 * Corresponds to a specification of a set of triples which can be executed via
 * {@code Stream.stream(graph.find(triplePattern)).filter(filter::test)}.
 *
 *
 * @author raven
 *
 */
public class TripleFilter
    implements Predicate<Triple>
{
    protected Triple triplePattern;

    /**
     * A conjunction of expressions
     */
    protected ExprList exprs;

    /** If isForward is true then the subject acts as the source and the object as the target.
     * otherwise its vice versa.
     */
    protected boolean isForward;

    public TripleFilter(Triple triplePattern, ExprList exprs, boolean isForward) {
        super();
        this.triplePattern = triplePattern;
        this.exprs = exprs;
        this.isForward = isForward;
    }

    public static TripleFilter create(Node source, Node predicate, boolean isForward) {
        return new TripleFilter(Triple.create(source, predicate, Vars.o), null, isForward);
    }

    public Node getSource() {
        return TripleUtils.getSource(triplePattern, isForward);
    }

    public Node getTarget() {
        return TripleUtils.getTarget(triplePattern, isForward);
    }

    public Triple getTriplePattern() {
        return triplePattern;
    }

//    public Triple getMatchPattern() {
//        Triple result = TripleUtils.createMatch(triplePattern, isForward);
//        return result;
//    }

    public ExprList getExprs() {
        return exprs;
    }

    public boolean isForward() {
        return isForward;
    }

//    public static TripleFilter bind(Triple assignment) {
//    	Binding binding = TripleUtils.tripleToBinding(pattern, assignment);
//    	TripleFilter result = null;
//    	if (binding != null) {
//    		result = bind()
//    	}
//    	return result;
//    }
//

    public Boolean evalExpr(Triple triple) {
        Boolean result = Boolean.TRUE;
        Binding binding = TripleUtils.tripleToBinding(triplePattern, triple);
        ExecutionContext execCxt = ExecutionContextUtils.createFunctionEnv();
        if (binding != null && exprs != null) {
            for (Expr expr : exprs) {
                NodeValue nv = ExprLib.evalOrNull(expr, binding, execCxt);
                if (nv == null) {
                    result = true;
                    break;
                } else if (nv.isBoolean() && nv.getBoolean() == false) {
                    result = Boolean.FALSE;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Create a new TripleFilter with the variables in the triple pattern and the
     * expression substituted w.r.t. the given binding.
     */
    public TripleFilter bind(Binding binding) {
        TripleFilter result = this;
        // XXX May want to check if the set of variables here and that of the binding intersect
        boolean isChanged = !binding.isEmpty();
        if (isChanged) {
            NodeTransform xform = NodeTransformLib2.wrapWithNullAsIdentity(n -> n.isVariable() ? binding.get((Var)n) : null);
            Triple newPattern = NodeTransformLib.transform(xform, triplePattern);
            ExprList newExprs = exprs == null ? null : new ExprList(exprs.getList().stream()
                    .map(expr -> expr.applyNodeTransform(xform))
                    .map(ExprLib::foldConstants)
                    .collect(Collectors.toList()));

            result = new TripleFilter(newPattern, newExprs, isForward);
        }
        return result;
    }

    public TripleFilter bindSource(Node value) {
        Node source = TripleUtils.getSource(triplePattern, isForward);
        BindingBuilder bb = BindingFactory.builder();
        boolean added = NodeUtils.put(bb, source, value);
        TripleFilter result = added ? bind(bb.build()) : null;
        return result;
    }

    public TripleFilter bindTarget(Node value) {
        Node target = TripleUtils.getTarget(triplePattern, isForward);
        BindingBuilder bb = BindingFactory.builder();
        boolean added = NodeUtils.put(bb, target, value);
        TripleFilter result = added ? bind(bb.build()) : null;
        return result;
    }

    /**
     * Convert this object to a slightly simplified representation which loses the 'direction' information.
     *
     * @return
     */
    public TripleConstraint toConstraint() {
        Triple pattern = TripleUtils.create(getSource(), triplePattern.getPredicate(), getTarget(), isForward());
        TripleConstraint result = TripleConstraintImpl.create(pattern, exprs == null ? null : ExprUtils.andifyBalanced(exprs));
        return result;
    }

    /** A convenience shorthand for toConstraint().test(triple) */
    @Override
    public boolean test(Triple triple) {
        TripleConstraint c = toConstraint();
        boolean result = c.test(triple);
        return result;
//        Triple pattern = TripleUtils.create(getSource(), getTarget(), getSource(), isForward());
//        Binding binding = TripleUtils.tripleToBinding(pattern, triple);
//        boolean result = ExprListUtils.evalEffectiveBoolean(exprs, binding);
//        return result;
    }

    @Override
    public String toString() {
        return "TripleFilter [triplePattern=" + triplePattern + ", exprs=" + exprs + ", isForward=" + isForward + "]";
    }
}

