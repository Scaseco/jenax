package org.aksw.jena_sparql_api.algebra.transform;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jenax.arq.util.syntax.VarExprListUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;

public class ProjectExtend {
    protected List<Var> project = null;
    protected VarExprList vel = null;
    protected Op subOp;

    protected Set<Var> pullableVars = new LinkedHashSet<>();
    protected Set<Var> nonPullableVars;

    public ProjectExtend(List<Var> project, VarExprList vel, Op subOp) {
        super();
        this.project = project;
        this.vel = vel;
        this.subOp = subOp;


        Collection<Var> vars = project != null ? project : vel.getVars();
        for (Var var : vars) {
            Expr expr = vel.getExpr(var);
            if (expr != null && expr.isConstant()) {
                pullableVars.add(var);
            }
        }

        nonPullableVars = vars.stream().filter(v -> !pullableVars.contains(v)).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<Var> getPullableVars() {
        return pullableVars;
    }

    public Set<Var> getNonPullableVars() {
        return nonPullableVars;
    }

    public List<Var> getProject() {
        return project;
    }

    public VarExprList getVel() {
        return vel;
    }

    public Op getSubOp() {
        return subOp;
    }

    public Op toOp() {
        return apply(subOp);
    }

    public Op apply(Op subOp) {
        Op result = subOp;
        if (vel != null) {
            result = OpExtend.create(result, vel);
        }

        if (project != null) {
            result = new OpProject(result, project);
        }
        return result;
    }

    public static Op apply(List<Var> project, VarExprList vel, Op subOp) {
        Op result = subOp;
        if (vel != null) {
            result = OpExtend.create(result, vel);
        }

        if (project != null) {
            result = new OpProject(result, project);
        }
        return result;
    }

    /**
     *
     * @param project If true, inject a OpProject with the variables in vel.
     * @param vel
     * @param subOp
     * @return
     */
    public static Op applyIfNeeded(boolean project, VarExprList vel, Op subOp) {
        Op result = subOp;
        if (!vel.isEmpty()) {
            VarExprList tmp = VarExprListUtils.projectExprsOnly(vel, vel.getVars());
            if (!tmp.isEmpty()) {
                result = OpExtend.create(result, tmp);
            }

            if (project) {
                result = new OpProject(result, vel.getVars());
            }
        }
        return result;
    }


    public static ProjectExtend collect(Op op) {
        List<Var> project = null;
        VarExprList vel = null;
        Op tmp = op;
        if (tmp instanceof OpProject) {
            OpProject o = (OpProject)tmp;
            project = o.getVars();
            tmp = o.getSubOp();
        }

        if (tmp instanceof OpExtend) {
            OpExtend o = (OpExtend)tmp;
            vel = o.getVarExprList();
            tmp = o.getSubOp();
        }

        ProjectExtend result = vel == null ? null : new ProjectExtend(project, vel, tmp);
        return result;
    }

    @Override
    public String toString() {
        String result = "" + apply(subOp);
        return result;
    }
}
