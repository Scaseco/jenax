package org.aksw.jenax.sparql.algebra.walker;

import java.util.LinkedHashMap;
import java.util.Map;

import org.aksw.commons.path.core.Path;
import org.aksw.jenax.constraint.api.ConstraintRow;
import org.aksw.jenax.constraint.impl.ConstraintRowMap;
import org.aksw.jenax.constraint.util.ConstraintUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.OpVisitorByTypeBase;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpUnion;

public class TrackingTransformExample
    extends TrackingTransformCopy<ConstraintRow>
{
    public class BeforeVisitor
        extends OpVisitorByTypeBase
    {
        @Override
        public void visit(OpBGP opBGP) {
            System.out.println("Path before bgp: " + path());
        }

        @Override
        public void visit(OpFilter op) {
            // get conditions from parent + merge with the current ones
            beforeConditions.put(path(), op.getExprs());
        }

        @Override
        public void visit(OpUnion opUnion) {
        }

    }


    protected BeforeVisitor beforeVisitor;


    protected Map<Path<String>, Object> beforeConditions = new LinkedHashMap<>();
    protected Map<Path<String>, Object> afterConditions = new LinkedHashMap<>();


    public TrackingTransformExample(Tracker pathState) {
        super(pathState);
        this.beforeVisitor = new BeforeVisitor();
    }

    @Override
    public OpVisitor getBeforeVisitor() {
        return beforeVisitor;
    }

    @Override
    public Op transform(OpProject opProject, Op subOp) {
        Path<String> path = tracker.getPath();
        System.out.println("Path at project: " + path);
        return super.transform(opProject, subOp);
    }

    @Override
    public Op transform(OpBGP opBGP) {
        ConstraintRow crow = tracker.computeIfAbsent(p -> ConstraintRowMap.create());

        for (Triple triple : opBGP.getPattern()) {
            ConstraintUtils.deriveConstraints(crow, triple);
        }

        Path<String> path = tracker.getPath();
        System.out.println("Path at bgp: " + path);

        System.out.println("Constraints: " + crow);
        return super.transform(opBGP);
    }


    @Override
    public Op transform(OpUnion opUnion, Op left, Op right) {
        if (path().getParent() != null) {
            beforeConditions.put(path(), beforeConditions.get(path().getParent()));
        }

        System.out.println("Conditions at " + path() + ": " + beforeConditions.get(path()));
        return super.transform(opUnion, left, right);
    }



}
