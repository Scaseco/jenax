package org.aksw.sparqlify.database;

import org.aksw.jena_sparql_api.restriction.RestrictionManagerImpl;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.sse.writers.WriterOp;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

public class OpExtFilterIndexed
	extends OpExt
{
	protected Op subOp;
	protected RestrictionManagerImpl restrictions;

	public Op getSubOp() {
		return subOp;
	}
	
	public RestrictionManagerImpl getRestrictions() {
		return restrictions;
	}

	public OpExtFilterIndexed(Op subOp, RestrictionManagerImpl restrictions) {
		super("OpExtFilterIndexed");
		this.subOp = subOp;
		this.restrictions = restrictions;
	}

	@Override
	public Op effectiveOp() {
		return OpFilter.filterBy(restrictions.getExprs(), subOp);
	}

	@Override
	public QueryIterator eval(QueryIterator input, ExecutionContext execCxt) {
		return null;
	}

	@Override
	public void outputArgs(IndentedWriter out, SerializationContext sCxt) {
        int line = out.getRow() ;
        out.println("[Restrictions: " + restrictions + "]");
        
        WriterOp.output(out, this.subOp, sCxt) ;
        
        if (line != out.getRow()) {
            out.ensureStartOfLine();
        }
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
        if ( ! (other instanceof OpExtFilterIndexed) ) return false ;
        OpExtFilterIndexed opFilter = (OpExtFilterIndexed)other ;
        if ( ! restrictions.equals(opFilter.restrictions) )
            return false ;
        
        return getSubOp().equalTo(opFilter.getSubOp(), labelMap) ;
	}
	
}