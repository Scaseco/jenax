package org.aksw.facete.v3.experimental;

import java.util.Collection;

import org.aksw.facete.v3.api.path.Resolver;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.data_query.api.ResolverDirNode;
import org.aksw.jena_sparql_api.data_query.api.ResolverMultiNode;
import org.aksw.jena_sparql_api.data_query.api.ResolverNode;
import org.aksw.jenax.sparql.relation.api.BinaryRelation;
import org.aksw.jenax.sparql.relation.query.PartitionedQuery1;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;

public class ResolverNodeImpl
    extends PathNode<ResolverNode, ResolverDirNode, ResolverMultiNode>
    implements ResolverNode
{
    protected Resolver resolver;
    protected DataQuery<?> conn;

//	public ResolverNode(Resolver resolver) {
//		super();
//		this.resolver = resolver;
//	}

    public ResolverNodeImpl(ResolverMultiNode parent, String alias, Resolver resolver, DataQuery<?> conn) {
        super(parent, alias);
        this.resolver = resolver;
        this.conn = conn;
    }

    public Resolver getResolver() {
        return resolver;
    }

    @Override
    public ResolverDirNode create(boolean isFwd) {
        return new ResolverDirNodeImpl(this, isFwd, conn);
    }

    @Override
    public Collection<BinaryRelation> getPaths() {
        Collection<BinaryRelation> result = resolver.getPaths();
        return result;
    }


    public static ResolverNodeImpl from(Resolver resolver, DataQuery<?> conn) {
        return new ResolverNodeImpl(null, null, resolver, conn);
    }

    public static ResolverNodeImpl from(PartitionedQuery1 pq, DataQuery<?> conn) {
        return from(Resolvers.from(pq), conn);
    }

    public static ResolverNodeImpl from(Query query, Node templateRootNode, DataQuery<?> conn) {
        return from(Resolvers.from(query, templateRootNode), conn);
    }


}