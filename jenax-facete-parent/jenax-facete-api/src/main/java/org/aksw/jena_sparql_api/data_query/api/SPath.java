package org.aksw.jena_sparql_api.data_query.api;

import org.aksw.jenax.sparql.relation.api.BinaryRelation;

public interface SPath
    extends Selection
{
    //ExprVar asExpr();

//	SPathNode getSource();
//	SPathNode getTarget();
    SPath getParent();


    String getPredicate();
    boolean isReverse();

    SPath get(String predicate, boolean reverse);

    BinaryRelation getReachingBinaryRelation();
//	void setParent(Resource source);
//	void setTarget(Resource target);

    //void setPredicate(Property p);
    //void setReverse(boolean isReverse);
}
