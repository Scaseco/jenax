package org.aksw.sparqlify.sparqlview;


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.jena_sparql_api.views.SparqlView;
import org.aksw.jena_sparql_api.views.VarBinding;
import org.aksw.jena_sparql_api.views.VarsMentioned;
import org.aksw.jena_sparql_api.views.ViewInstance;
import org.aksw.sparqlify.database.GetVarsMentioned;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;


public class OpSparqlViewPattern
    extends OpExt
    implements VarsMentioned
{
    private ViewInstanceJoin<SparqlView> conjunction;

    public OpSparqlViewPattern(ViewInstanceJoin<SparqlView> conjunction) {
        super(OpSparqlViewPattern.class.getSimpleName());
        this.conjunction = conjunction;
    }

    /*
    public OpRdfViewPattern() {
        super(OpRdfUnionViewPattern.class.getName());
        this.conjunction = new ArrayList<RdfViewConjunction>();
    }*/

    public ViewInstanceJoin<SparqlView> getConjunction() {
        return conjunction;
    }

    @Override
    public Op effectiveOp() {
        Op a = null;
        Op b = null;
        for(ViewInstance<SparqlView> instance : conjunction.getViewInstances()) {
            b = instance.getViewDefinition().getOp();

            //Map<Node, N>
            //Map<Var, Collection<Var>> viewToQueryVar = conjunction.getCompleteBinding().getEquiMap().getEquivalences().getInverse().asMap();
            //instance.getRenamer()

            Map<Var, Var> renamer = new HashMap<Var, Var>();

            // TODO I think we don't need constraints here???
            Set<Expr> constraints = new HashSet<Expr>();

            // The same view variable might server multiple query variables
            SetMultimap<Var, Var> extraProjection = HashMultimap.create();

            //Multimap<Var, Var> src = instance.getVarDefinition();

            VarBinding varBinding = instance.getBinding();

            //SetMultimap<Var, Var> src = instance.getQueryToParentBinding();
            //SetMultimap<Var, Var> dest = HashMultimap.create();
            IBiSetMultimap<Var, Var> dest = varBinding.getViewVarToQueryVars();

            //Multimaps.invertFrom(src, dest);

            for(Entry<Var, Collection<Var>> entry : dest.asMap().entrySet()) {

                // Add equals constraint if multiple view variables map to the same query variable
                Var x = null;
                for(Var y : entry.getValue()) {
                    if(x == null) {
                        x = y;
                        continue;
                    }

                    //constraints.add(new E_Equals(new ExprVar(x), new ExprVar(y)));
                    extraProjection.put(x, y);
                }

                // Rename the view variable to the query variable
                renamer.put(entry.getKey(), x);
            }

            SparqlView renamed = instance.getViewDefinition().copyRenameVars(renamer); //NodeTransformLib.transform(renamer, op);

            b = renamed.getOp();

            //Op
            VarExprList veList = new VarExprList();


            Map<Var, Node> keyToValue = varBinding.getQueryVarToConstant();

            if(!extraProjection.isEmpty() || !keyToValue.isEmpty()) {
                //b = OpFilter.filter(new ExprList(new ArrayList<Expr>(constraints)), b);
                for(Entry<Var, Var> entry : extraProjection.entries()) {
                    //veList.add(entry.getKey(), new ExprVar(entry.getValue()));
                    veList.add(entry.getValue(), new ExprVar(entry.getKey()));
                }


                for(Entry<Var, Node> entry : keyToValue.entrySet()) {
                    /*
                    if(entry.getValue().equals(Quad.defaultGraphNodeGenerated)) {
                        System.out.println("[Hack] Discarding default graph URI constraint, although it might affect a SPARQL variable in non-graph position");
                        continue;
                    }*/
                    Node node = entry.getValue();
                    if(node != null) {
                        veList.add(entry.getKey(), NodeValue.makeNode(node));
                    }
                    //veList.add(entry.getKey(), NodeValue.makeNode(entry.getValue()));
                }

                if(!veList.isEmpty()) {
                    b = OpExtend.extend(b, veList);
                }
            }

            //System.out.println(renamed);

            if(a == null) {
                a = b;
                continue;
            }


            a = OpJoin.create(a, b);
        }

        return a;
    }

    @Override
    public QueryIterator eval(QueryIterator input, ExecutionContext execCxt) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void outputArgs(IndentedWriter out, SerializationContext sCxt) {
        /*
         * out.print("'''") ; sqlNode.output(out) ; out.print("'''") ;
         */
        out.print(conjunction.getViewNames() + " " + conjunction.getRestrictions() + " " + conjunction);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((conjunction == null) ? 0 : conjunction.hashCode());
        return result;
    }

    @Override
    public boolean equalTo(Op obj, NodeIsomorphismMap labelMap)
    {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        OpSparqlViewPattern other = (OpSparqlViewPattern) obj;
        if (conjunction == null) {
            if (other.conjunction != null)
                return false;
        } else if (!conjunction.equals(other.conjunction))
            return false;
        return true;
    }

    @Override
    public Set<Var> varsMentioned() {
        return GetVarsMentioned.getVarsMentioned(effectiveOp());
    }

}

