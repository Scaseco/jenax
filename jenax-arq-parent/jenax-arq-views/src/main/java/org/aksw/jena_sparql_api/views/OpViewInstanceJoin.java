package org.aksw.jena_sparql_api.views;

import org.aksw.sparqlify.sparqlview.ViewInstanceJoin;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

public class OpViewInstanceJoin<T extends IViewDef>
	extends OpExt
{
	private ViewInstanceJoin<T> join;

	public OpViewInstanceJoin(ViewInstanceJoin<T> join) {
		super("view instance join");
		this.join = join;
	}

	public ViewInstanceJoin<T> getJoin() {
		return join;
	}

	@Override
	public Op effectiveOp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryIterator eval(QueryIterator input, ExecutionContext execCxt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void outputArgs(IndentedWriter out, SerializationContext sCxt) {
		// TODO Auto-generated method stub
		out.print(join.getViewNames() + " " + join.getRestrictions());		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((join == null) ? 0 : join.hashCode());
		return result;
	}

	@Override
	public boolean equalTo(Op obj, NodeIsomorphismMap labelMap) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		OpViewInstanceJoin<T> other = (OpViewInstanceJoin<T>) obj;
		if (join == null) {
			if (other.join != null)
				return false;
		} else if (!join.equals(other.join))
			return false;
		return true;
	}
}
