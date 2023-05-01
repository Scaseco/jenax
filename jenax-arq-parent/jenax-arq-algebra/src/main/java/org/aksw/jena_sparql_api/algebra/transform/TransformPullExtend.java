package org.aksw.jena_sparql_api.algebra.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jenax.arq.util.syntax.VarExprListUtils;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLateral;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.algebra.optimize.TransformExtendCombine;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;


class ProjectExtend {
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

/**
 * Pulls up Extend over joins and distinct
 * join(extend(?foo...), extend(?bar...)) becomes
 * extend(?foo... ?bar..., join(...))
 *
 * If for example the resulting join is between two BGPs, it allows for nicely merging them afterwards.
 * In conjunction with TransformPullFiltersIfCanMergeBGPs this is even more powerful.
 *
 * Also merges OpProject(OpProject(op, varsInner), varsOuter) into OpProject(op, intersection(varsInner, varsOuter))
 *
 *
 * @author raven
 *
 */
public class TransformPullExtend
    // extends TransformCopy
    extends TransformExtendCombine
{
    public static Op transform(Op op) {
        Transform transform = new TransformPullExtend();
        Op result = Transformer.transform(transform, op);
        //Op result = FixpointIteration.apply(op, o -> Transformer.transform(transform, o));
        //Op result = Transformer.transform(transform, op);
        return result;
    }


    @Override
    public Op transform(OpDisjunction opDisjunction, List<Op> elts) {
        Op result = transformDisjunction(elts, list -> OpUnion.create(list.get(0), list.get(1)));
        if (result == null) {
            result = super.transform(opDisjunction, elts);
        }
        return result;
    }

    @Override
    public Op transform(OpUnion opUnion, Op left, Op right) {
        Op result = transformDisjunction(Arrays.asList(left, right), list -> OpUnion.create(list.get(0), list.get(1)));
        if (result == null) {
            result = super.transform(opUnion, left, right);
        }
        return result;
    }


    @Override
    public Op transform(OpProject opProject, Op subOp) {
        Op result;
        if (subOp instanceof OpProject) {
            OpProject o = (OpProject)subOp;
            Set<Var> vars = new HashSet<>(o.getVars());
            List<Var> newVars = opProject.getVars().stream().filter(vars::contains).collect(Collectors.toList());
            result = new OpProject(o.getSubOp(), newVars);
        } else {
            result = super.transform(opProject, subOp);
        }
        return result;
    }

    public Op transformDisjunction(List<Op> subOps, Function<List<Op>, Op> toUnion) {
        Op result = null; // null unless change

        VarExprList common = new VarExprList();

        List<ProjectExtend> pes = subOps.stream().map(ProjectExtend::collect).collect(Collectors.toList());
        boolean allMembersProcessable = !pes.stream().anyMatch(Objects::isNull);

        if (allMembersProcessable) {

            // The set of variables that are _candidates_ for pulling
            Set<Var> candidatePullableVars = pes.stream().map(ProjectExtend::getPullableVars).flatMap(Collection::stream).collect(Collectors.toSet());

            Set<Var> rejectedCandidateVars = new HashSet<>();
            // Iterator<Var> it = commonPullableVars.iterator();
            nextVar: for (Var var : candidatePullableVars) {
                boolean isPriorSet = false;
                Expr prior = null;
                for (ProjectExtend pe : pes) {
                    Expr expr = pe.getVel().getExpr(var);
                    if (!isPriorSet) {
                        isPriorSet = true;
                        prior = expr;
                    } else if (!Objects.equals(prior, expr)) {
                        rejectedCandidateVars.add(var);
                        continue nextVar;
                    }
                }
                if (prior != null) {
                    common.add(var, prior);
                }
            }

            // Note: We need to add non-pullable vars back

            if (!common.isEmpty()) {
                List<Op> newOps = new ArrayList<>(subOps.size());
                for (ProjectExtend pe : pes) {
                    List<Var> nonPullableVars = new ArrayList<>(pe.getNonPullableVars());
                    nonPullableVars.addAll(rejectedCandidateVars);

                    VarExprList nonPullableVel = VarExprListUtils.projectAllVars(pe.getVel(), nonPullableVars);
                    Op newInnerOp = ProjectExtend.applyIfNeeded(pe.getProject() != null, nonPullableVel, pe.getSubOp());
                    // Op newDistinct = OpDistinct.create(newInnerOp);
                    newOps.add(newInnerOp);
                }

                Op newDisjunction = toUnion.apply(newOps);

                VarExprList pullable = VarExprListUtils.projectAllVars(common, common.getVars());
                result = ProjectExtend.applyIfNeeded(false, pullable, newDisjunction);
            }
        }

        return result;
    }


    @Override
    public Op transform(OpLateral op, Op left, Op right) {
        // Probably we don't need to check for 'join' vars because the rhs is probably prohibited to BIND visibles variables that are in-scope on the lhs
        // Set<Var> vvLeft = OpVars.visibleVars(left);
        // Set<Var> vvRight = OpVars.visibleVars(right);
        // However, we must not pull variables from the rhs that are defined in terms of lhs ones

        //ProjectExtend peLeft = ProjectExtend.collect(left);
        ProjectExtend peRight = ProjectExtend.collect(right);

        Op result = null;
        if (peRight != null) {
            VarExprList nonPullable = VarExprListUtils.projectAllVars(peRight.getVel(), peRight.getNonPullableVars());
            Op newInnerRight = ProjectExtend.applyIfNeeded(peRight.getProject() != null, nonPullable, peRight.getSubOp());

            // Special case If the innerRight is merely OpExtend(unit) then we can omit the lateral
            // X LATERAL { BIND(?bar) BIND(?baz) } becomes X BIND(?bar) BIND(?baz)
            if (newInnerRight instanceof OpExtend) {
                OpExtend o = (OpExtend)newInnerRight;
                if (OpJoin.isJoinIdentify(o.getSubOp())) {
                    Op newLateral = OpExtend.create(left, o.getVarExprList());
                    VarExprList pullable = VarExprListUtils.projectAllVars(peRight.getVel(), peRight.getPullableVars());
                    result = ProjectExtend.apply(peRight.getProject(), pullable, newLateral);
                }
            }

            if (result == null) {
                Op newLateral = OpLateral.create(left, newInnerRight);
                VarExprList pullable = VarExprListUtils.projectAllVars(peRight.getVel(), peRight.getPullableVars());
                result = ProjectExtend.apply(peRight.getProject(), pullable, newLateral);
            }
        } else {
            result = super.transform(op, left, right);
        }
        return result;
    }

    @Override
    public Op transform(OpSlice op, Op subOp) {
        ProjectExtend pe = ProjectExtend.collect(subOp);
        Op result;
        if (pe != null) {
            result = pe.apply(new OpSlice(pe.getSubOp(), op.getStart(), op.getLength()));
        } else {
            result = super.transform(op, subOp);
        }
        return result;
    }

    @Override
    public Op transform(OpDistinct op, Op subOp) {
        Op result;
        ProjectExtend pe = ProjectExtend.collect(subOp);

        if (pe != null && !pe.getPullableVars().isEmpty()) {
            VarExprList nonPullable = VarExprListUtils.projectAllVars(pe.getVel(), pe.getNonPullableVars());
            Op newInnerOp = ProjectExtend.applyIfNeeded(pe.getProject() != null, nonPullable, pe.getSubOp());
            Op newDistinct = OpDistinct.create(newInnerOp);
            VarExprList pullable = VarExprListUtils.projectAllVars(pe.getVel(), pe.getPullableVars());
            result = ProjectExtend.apply(pe.getProject(), pullable, newDistinct);
        } else {
            result = super.transform(op, subOp);
        }
        return result;
    }

    // Filter(Extend(X, e), f) -> Extend(Filter(X, f), e) if filter does not depend on any variable
    // of e
    @Override
    public Op transform(OpFilter opFilter, Op subOp) {
        Op result = null;

        // TODO We can do alot better here - this is old code that does not yet use ProjectExtend

        if(subOp instanceof OpExtend) {
            OpExtend e = (OpExtend)subOp;

            VarExprList vel = e.getVarExprList();
            Set<Var> extendVars = VarExprListUtils.getVarsMentioned(vel);

            ExprList el = opFilter.getExprs();
            Set<Var> usedVars = el.getVarsMentioned();


            Set<Var> conflicts = Sets.intersection(extendVars, usedVars);
            if(conflicts.isEmpty()) {
                boolean containsSpecialVars =
                        TransformPullFiltersIfCanMergeBGPs.containsSpecialVar(extendVars) ||
                        TransformPullFiltersIfCanMergeBGPs.containsSpecialVar(usedVars);

                if(!containsSpecialVars) {
                    result = OpExtend.create(OpFilter.filterBy(el, e.getSubOp()), vel);
                }
            }

        }

        if(result == null) {
            result = super.transform(opFilter, subOp);
        }

        return result;
    }

    @Override
    public Op transform(OpJoin opJoin, Op left, Op right) {
        OpExtend ol = left instanceof OpExtend ? ((OpExtend)left) : null;
        OpExtend or = right instanceof OpExtend ? ((OpExtend)right) : null;
        VarExprList velLeft = ol != null ? ol.getVarExprList() : null;
        VarExprList velRight = or != null ? or.getVarExprList() : null;

        Op result;
        if(velLeft != null && velRight != null) {
            Set<Var> conflicts = Sets.intersection(
                new LinkedHashSet<>(velLeft.getVars()),
                new LinkedHashSet<>(velRight.getVars()));

            if(conflicts.isEmpty()) {
                VarExprList combined = new VarExprList();
                combined.addAll(velLeft);
                combined.addAll(velRight);

                result = OpExtend.extend(OpJoin.create(ol.getSubOp(), or.getSubOp()), combined);
            } else {
                // TODO We could pull up all non conflicting binds
                // Also, we could even create a filter FALSE for conflicting vars
                // But for now we don't bother
                result = super.transform(opJoin, left, right);
            }
        } else if(velLeft != null) {
            result = OpExtend.extend(OpJoin.create(ol.getSubOp(), right), velLeft);
        } else if(velRight != null) {
            result = OpExtend.extend(OpJoin.create(left, or.getSubOp()), velRight);
        } else {
            result = super.transform(opJoin, left, right);
        }

        return result;
    }



    public static void main(String[] args) {
        String str = "SELECT * WHERE\n"
                + "  { SELECT DISTINCT  ?__g__ ?__s__ ?__p__ ?__o__\n"
                + "    WHERE\n"
                + "      { { SELECT  ?jc0 ?v4\n"
                + "          WHERE\n"
                + "            { SERVICE <https://w3id.org/aksw/sparqlx#rml.source>\n"
                + "                { <https://w3id.org/aksw/sparqlx#rml.source>\n"
                + "                            a                     <http://semweb.mmlab.be/ns/rml#LogicalSource> ;\n"
                + "                            <http://semweb.mmlab.be/ns/rml#referenceFormulation>  <http://semweb.mmlab.be/ns/ql#CSV> ;\n"
                + "                            <http://semweb.mmlab.be/ns/rml#source>  \"ROUTES.csv\" ;\n"
                + "                            <https://w3id.org/function/ontology#returns>  ?s0\n"
                + "                }\n"
                + "              BIND(<http://jsa.aksw.org/fn/json/path>(?s0, \"$['agency_id']\") AS ?jc0)\n"
                + "              FILTER bound(?jc0)\n"
                + "              BIND(IRI(concat(\"http://transport.linkeddata.es/madrid/metro/routes/\", encode_for_uri(str(<http://jsa.aksw.org/fn/json/path>(?s0, \"$['route_id']\"))))) AS ?v4)\n"
                + "            }\n"
                + "          LIMIT   9223372036854775807\n"
                + "        }\n"
                + "        { SELECT  ?jc0 ?s1_v4\n"
                + "          WHERE\n"
                + "            { SERVICE <https://w3id.org/aksw/sparqlx#rml.source>\n"
                + "                { <https://w3id.org/aksw/sparqlx#rml.source>\n"
                + "                            a                     <http://semweb.mmlab.be/ns/rml#LogicalSource> ;\n"
                + "                            <http://semweb.mmlab.be/ns/rml#referenceFormulation>  <http://semweb.mmlab.be/ns/ql#CSV> ;\n"
                + "                            <http://semweb.mmlab.be/ns/rml#source>  \"AGENCY.csv\" ;\n"
                + "                            <https://w3id.org/function/ontology#returns>  ?s1\n"
                + "                }\n"
                + "              BIND(<http://jsa.aksw.org/fn/json/path>(?s1, \"$['agency_id']\") AS ?jc0)\n"
                + "              FILTER bound(?jc0)\n"
                + "              BIND(IRI(concat(\"http://transport.linkeddata.es/madrid/agency/\", encode_for_uri(str(<http://jsa.aksw.org/fn/json/path>(?s1, \"$['agency_id']\"))))) AS ?s1_v4)\n"
                + "            }\n"
                + "          LIMIT   9223372036854775807\n"
                + "        }\n"
                + "        LATERAL {\n"
                + "          { BIND(<urn:x-arq:DefaultGraphNode> AS ?__g__)\n"
                + "            BIND(?v4 AS ?__s__)\n"
                + "            BIND(<http://vocab.gtfs.org/terms#agency> AS ?__p__)\n"
                + "            BIND(?s1_v4 AS ?__o__)\n"
                + "          } UNION \n"
                + "          { BIND(<urn:x-arq:DefaultGraphNode> AS ?__g__)\n"
                + "            BIND(?v1_v4 AS ?__s__)\n"
                + "            BIND(<http://vocab.gtfs.org/terms#agency> AS ?__p__)\n"
                + "            BIND(?v4 AS ?__o__)\n"
                + "          }\n"
                + "      } } \n"
                + "  }\n"
                + "";

        Query query = QueryFactory.create(str);
        System.out.println(query);
        Op op = Algebra.compile(query);
        //System.out.println(op);

        op = Transformer.transform(new TransformPullExtend(), op);
        // System.out.println(op);


        Query newQuery = OpAsQuery.asQuery(op);
        System.out.println(newQuery);
    }
}
