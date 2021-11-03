package org.aksw.sparqlify.database;

import org.aksw.jena_sparql_api.restriction.RestrictionManagerImpl;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.util.NodeIsomorphismMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpFilterIndexed
    extends Op1
{

    private static final Logger logger = LoggerFactory.getLogger(OpFilterIndexed.class);

    protected RestrictionManagerImpl restrictions;

    public RestrictionManagerImpl getRestrictions() {
        return restrictions;
    }

    public OpFilterIndexed(Op subOp, RestrictionManagerImpl restrictions) {
        super(subOp);
        this.restrictions = restrictions;
    }



    /*
    @Override
    public void output(IndentedWriter out)
    {
        super.output(out);
        //OpFilter x;
        //out.println(getName());
        //QueryOutputUtils.output(this, out) ;
    }*/


    private static boolean hackWarningDisplayed = false;
    @Override
    public void visit(OpVisitor opVisitor) {

        if(!hackWarningDisplayed) {
            logger.warn("[HACK] Replace OpFilterIndexed with OpExtFilterIndexed");
            hackWarningDisplayed = true;
        }

        OpExtFilterIndexed tmp = new OpExtFilterIndexed(this.getSubOp(), this.getRestrictions());
        tmp.visit(opVisitor);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Op apply(Transform transform, Op subOp) {
        //return transform.transform(this, subOp) ;
        return null;
    }

    @Override
    public Op1 copy(Op subOp) {
        return OpFilterIndexed.filter(new RestrictionManagerImpl(restrictions), subOp);
    }

    @Override
    public int hashCode() {
        return 17 * getSubOp().hashCode() + 13 * restrictions.hashCode();
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
        if ( ! (other instanceof OpFilterIndexed) ) return false ;
        OpFilterIndexed opFilter = (OpFilterIndexed)other ;
        if ( ! restrictions.equals(opFilter.restrictions) )
            return false ;

        return getSubOp().equalTo(opFilter.getSubOp(), labelMap) ;
    }


    public static OpFilterIndexed filter(RestrictionManagerImpl restrictions, Op subOp) {
        return new OpFilterIndexed(subOp, restrictions);
    }
}