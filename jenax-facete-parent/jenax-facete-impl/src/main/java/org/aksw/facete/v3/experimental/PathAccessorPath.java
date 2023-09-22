package org.aksw.facete.v3.experimental;

import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.data_query.api.PathAccessorRdf;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.relation.api.BinaryRelation;
import com.google.common.collect.Iterables;
import org.apache.jena.sparql.path.P_Path0;

//
public abstract class PathAccessorPath<P>
    implements PathAccessorRdf<P>
{
//	@Override
//	public P getParent(P path) {
//		// TODO Auto-generated method stub
//		return null;
//	}
    protected abstract P_Path0 getLastStep(P path);

    @Override
    public BinaryRelation getReachingRelation(P path) {
        P_Path0 step = getLastStep(path);//Iterables.getLast(null);

        BinaryRelation result = BinaryRelationImpl.createFwd(Vars.s, step.getNode(), Vars.o);
        return result;
    }

    @Override
    public boolean isReverse(P path) {
        P_Path0 step = getLastStep(path); //Iterables.getLast(null);
        boolean result = !step.isForward();
        return result;
    }

    @Override
    public String getPredicate(P path) {
        P_Path0 step = Iterables.getLast(null);
        String result = step.getNode().getURI();
        return result;
    }

    @Override
    public String getAlias(P path) {
        return null;
    }
}
//
//
//class PathAccessorSimplePath
//	extends PathAccessorPath<SimplePath>
//{
//	@Override
//	public SimplePath getParent(SimplePath path) {
//		return path.parentPath();
//	}
//
//	@Override
//	protected P_Path0 getLastStep(SimplePath path) {
//		return path.lastStep();
//	}
//}