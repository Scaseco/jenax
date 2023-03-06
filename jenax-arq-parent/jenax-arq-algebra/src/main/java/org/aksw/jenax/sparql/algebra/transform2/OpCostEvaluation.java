package org.aksw.jenax.sparql.algebra.transform2;

import java.util.List;
import java.util.stream.IntStream;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpConditional;
import org.apache.jena.sparql.algebra.op.OpDatasetNames;
import org.apache.jena.sparql.algebra.op.OpDiff;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLabel;
import org.apache.jena.sparql.algebra.op.OpLateral;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpList;
import org.apache.jena.sparql.algebra.op.OpMinus;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpPath;
import org.apache.jena.sparql.algebra.op.OpProcedure;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpPropFunc;
import org.apache.jena.sparql.algebra.op.OpQuad;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpReduced;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.op.OpTopN;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.algebra.op.OpUnion;

public class OpCostEvaluation
    implements Evaluation<OpCost>
{
    protected float UNKNOWN = 10_000_000;

    @Override
    public OpCost eval(OpTable op) {
        OpCost result = new OpCost(op, op.getTable().size());
        return result;
    }

    @Override
    public OpCost eval(OpBGP op) {
        OpCost result = new OpCost(op, UNKNOWN);
        return result;
    }

    @Override
    public OpCost eval(OpTriple op) {
        OpCost result = new OpCost(op, UNKNOWN);
        return result;
    }

    @Override
    public OpCost eval(OpQuad op) {
        OpCost result = new OpCost(op, UNKNOWN);
        return result;
    }

    @Override
    public OpCost eval(OpPath op) {
        OpCost result = new OpCost(op, UNKNOWN);
        return result;
    }

    @Override
    public OpCost eval(OpDatasetNames op) {
        OpCost result = new OpCost(op, UNKNOWN);
        return result;
    }

    @Override
    public OpCost eval(OpQuadPattern op) {
        OpCost result = new OpCost(op, UNKNOWN);
        return result;
    }

    @Override
    public OpCost eval(OpQuadBlock op) {
        OpCost result = new OpCost(op, UNKNOWN);
        return result;
    }

    @Override
    public OpCost eval(OpNull op) {
        OpCost result = new OpCost(op, UNKNOWN);
        return result;
    }

    @Override
    public OpCost eval(OpFilter op, OpCost subCost) {
        Op newOp = op.getSubOp() == subCost.getOp() ? op : OpFilter.filterBy(op.getExprs(), subCost.getOp());
        OpCost result = new OpCost(newOp, subCost.getCost() * 0.1f);
        return result;
    }

    @Override
    public OpCost eval(OpGraph op, OpCost subCost) {
        Op newOp = op.getSubOp() == subCost.getOp() ? op : new OpGraph(op.getNode(), subCost.getOp());
        OpCost result = new OpCost(newOp, subCost.getCost());
        return result;
    }

    @Override
    public OpCost eval(OpService op, OpCost subCost) {
        Op newOp = op.getSubOp() == subCost.getOp() ? op : new OpService(op.getService(), subCost.getOp(), op.getSilent());
        OpCost result = new OpCost(newOp, subCost.getCost());
        return result;
    }

    @Override
    public OpCost eval(OpProcedure op, OpCost subCost) {
        Op newOp = op.getSubOp() == subCost.getOp() ? op : new OpProcedure(op.getProcId(), op.getArgs(), subCost.getOp());
        OpCost result = new OpCost(newOp, subCost.getCost());
        return result;
    }

    @Override
    public OpCost eval(OpPropFunc op, OpCost subCost) {
        Op newOp = op.getSubOp() == subCost.getOp() ? op : new OpPropFunc(op.getProperty(), op.getSubjectArgs(), op.getObjectArgs(), subCost.getOp());
        OpCost result = new OpCost(newOp, subCost.getCost());
        return result;
    }

    @Override
    public OpCost eval(OpLabel op, OpCost subCost) {
        Op newOp = op.getSubOp() == subCost.getOp() ? op : OpLabel.create(op.getObject(), subCost.getOp());
        OpCost result = new OpCost(newOp, subCost.getCost());
        return result;
    }

    @Override
    public OpCost eval(OpAssign op, OpCost subCost) {
        Op newOp = op.getSubOp() == subCost.getOp() ? op : OpAssign.create(subCost.getOp(), op.getVarExprList());
        OpCost result = new OpCost(newOp, subCost.getCost());
        return result;
    }

    @Override
    public OpCost eval(OpExtend op, OpCost subCost) {
        Op newOp = op.getSubOp() == subCost.getOp() ? op : OpExtend.create(subCost.getOp(), op.getVarExprList());
        OpCost result = new OpCost(newOp, subCost.getCost());
        return result;
    }

    @Override
    public OpCost eval(OpJoin op, OpCost left, OpCost right) {
        Op newOp = op.getLeft() == left.getOp() && op.getRight() == right.getOp() ? op : OpJoin.create(left.getOp(), right.getOp());
        OpCost result = new OpCost(newOp, left.getCost() * right.getCost());
        return result;
    }

    @Override
    public OpCost eval(OpLeftJoin op, OpCost left, OpCost right) {
        Op newOp = op.getLeft() == left.getOp() && op.getRight() == right.getOp() ? op : OpLeftJoin.create(left.getOp(), right.getOp(), op.getExprs());
        OpCost result = new OpCost(newOp, left.getCost() * right.getCost());
        return result;
    }

    @Override
    public OpCost eval(OpDiff op, OpCost left, OpCost right) {
        Op newOp = op.getLeft() == left.getOp() && op.getRight() == right.getOp() ? op : OpDiff.create(left.getOp(), right.getOp());
        // TODO Assumes indexing of the rhs and n lookups with the rhs
        OpCost result = new OpCost(newOp, left.getCost() * (float)Math.log(right.getCost()));
        return result;
    }

    @Override
    public OpCost eval(OpMinus op, OpCost left, OpCost right) {
        Op newOp = op.getLeft() == left.getOp() && op.getRight() == right.getOp() ? op : OpMinus.create(left.getOp(), right.getOp());
        // TODO Assumes indexing of the rhs and n lookups with the rhs
        OpCost result = new OpCost(newOp, left.getCost() * (float)Math.log(right.getCost()));
        return result;
    }

    @Override
    public OpCost eval(OpUnion op, OpCost left, OpCost right) {
        Op newOp = op.getLeft() == left.getOp() && op.getRight() == right.getOp() ? op : OpUnion.create(left.getOp(), right.getOp());
        OpCost result = new OpCost(newOp, left.getCost() + right.getCost());
        return result;
    }

    @Override
    public OpCost eval(OpLateral op, OpCost left, OpCost right) {
        Op newOp = op.getLeft() == left.getOp() && op.getRight() == right.getOp() ? op : OpLateral.create(left.getOp(), right.getOp());
        OpCost result = new OpCost(newOp, left.getCost() * right.getCost());
        return result;
    }

    @Override
    public OpCost eval(OpConditional op, OpCost left, OpCost right) {
        Op newOp = op.getLeft() == left.getOp() && op.getRight() == right.getOp() ? op : new OpConditional(left.getOp(), right.getOp());
        OpCost result = new OpCost(newOp, left.getCost() * right.getCost());
        return result;
    }

    @Override
    public OpCost eval(OpSequence op, List<OpCost> elts) {
        List<Op> oldSubOps = op.getElements();
        boolean noChange = IntStream.range(0, elts.size()).allMatch(i -> oldSubOps.get(i) == elts.get(i).getOp());
        Op newOp;
        if (noChange) {
            newOp = op;
        } else {
            OpSequence tmp = OpSequence.create();
            elts.forEach(elt -> tmp.add(elt.getOp()));
            newOp = tmp;
        }
        OpCost result = new OpCost(newOp, (float)elts.stream().mapToDouble(OpCost::getCost).reduce(1, (before, cost) -> (float)before * cost));
        return result;
    }

    @Override
    public OpCost eval(OpDisjunction op, List<OpCost> elts) {
        List<Op> oldSubOps = op.getElements();
        boolean noChange = IntStream.range(0, elts.size()).allMatch(i -> oldSubOps.get(i) == elts.get(i).getOp());
        Op newOp;
        if (noChange) {
            newOp = op;
        } else {
            OpDisjunction tmp = OpDisjunction.create();
            elts.forEach(elt -> tmp.add(elt.getOp()));
            newOp = tmp;
        }
        OpCost result = new OpCost(newOp, (float)elts.stream().mapToDouble(OpCost::getCost).sum());
        return result;
    }

    @Override
    public OpCost eval(OpExt op) {
        OpCost result = new OpCost(op, UNKNOWN);
        return result;
    }

    @Override
    public OpCost eval(OpList op, OpCost subCost) {
        Op newOp = op.getSubOp() == subCost.getOp() ? op : new OpList(subCost.getOp());
        OpCost result = new OpCost(newOp, subCost.getCost());
        return result;
    }

    @Override
    public OpCost eval(OpOrder op, OpCost subCost) {
        Op newOp = op.getSubOp() == subCost.getOp() ? op : new OpOrder(subCost.getOp(), op.getConditions());
        float baseCost = subCost.getCost();
        OpCost result = new OpCost(newOp, baseCost * (1 + (float)Math.log(baseCost)));
        return result;
    }

    @Override
    public OpCost eval(OpTopN op, OpCost subCost) {
        Op newOp = op.getSubOp() == subCost.getOp() ? op : new OpTopN(subCost.getOp(), op.getLimit(), op.getConditions());
        // Adjust cost?
        OpCost result = new OpCost(newOp, subCost.getCost());
        return result;
    }

    @Override
    public OpCost eval(OpProject op, OpCost subCost) {
        Op newOp = op.getSubOp() == subCost.getOp() ? op : new OpProject(subCost.getOp(), op.getVars());
        OpCost result = new OpCost(newOp, subCost.getCost());
        return result;
    }

    @Override
    public OpCost eval(OpDistinct op, OpCost subCost) {
        Op newOp = op.getSubOp() == subCost.getOp() ? op : OpDistinct.create(subCost.getOp());
        float baseCost = subCost.getCost();
        OpCost result = new OpCost(newOp, baseCost * (1 + (float)Math.log(baseCost)));
        return result;
    }

    @Override
    public OpCost eval(OpReduced op, OpCost subCost) {
        Op newOp = op.getSubOp() == subCost.getOp() ? op : OpReduced.create(subCost.getOp());
        float baseCost = subCost.getCost();
        OpCost result = new OpCost(newOp, baseCost * (1 + (float)Math.log(baseCost)));
        return result;
    }

    @Override
    public OpCost eval(OpSlice op, OpCost subCost) {
        Op newOp = op.getSubOp() == subCost.getOp() ? op : new OpSlice(subCost.getOp(), op.getStart(), op.getLength());
        OpCost result = new OpCost(newOp, subCost.getCost());
        return result;
    }

    @Override
    public OpCost eval(OpGroup op, OpCost subCost) {
        Op newOp = op.getSubOp() == subCost.getOp() ? op : new OpGroup(subCost.getOp(), op.getGroupVars(), op.getAggregators());
        // Double the cost because we need to touch every binding
        OpCost result = new OpCost(newOp, 2 * subCost.getCost());
        return result;
    }
}
