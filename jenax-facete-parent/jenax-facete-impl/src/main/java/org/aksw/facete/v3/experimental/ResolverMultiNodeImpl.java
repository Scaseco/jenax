package org.aksw.facete.v3.experimental;

import org.aksw.facete.v3.api.path.Resolver;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.data_query.api.ResolverDirNode;
import org.aksw.jena_sparql_api.data_query.api.ResolverMultiNode;
import org.aksw.jena_sparql_api.data_query.api.ResolverNode;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_ReverseLink;

public class ResolverMultiNodeImpl
    extends PathMultiNode<ResolverNode, ResolverDirNode, ResolverMultiNode>
    implements ResolverMultiNode
{
    protected Resolver resolver;
    protected DataQuery<?> conn;
//	protected Map<String, ResolverNode> aliasToNode = new LinkedHashMap<>();


    public ResolverMultiNodeImpl(ResolverDirNodeImpl parent, Resource property, DataQuery<?> conn) {
        super(parent, property);
        this.resolver = parent.getResolver();
        this.conn = conn;
    }

    public Resolver getResolver() {
        return resolver;
    }

    @Override
    protected ResolverNodeImpl viaImpl(String alias) {
        Node n = property.asNode();
        P_Path0 step = isFwd ? new P_Link(n) : new P_ReverseLink(n);
        Resolver child = resolver.resolve(step, alias);
        ResolverNodeImpl result = new ResolverNodeImpl(this, alias, child, conn);
        return result;
    }
//	@Override
//	public Map<String, ResolverNode> list() {
//		return aliasToNode;
//	}

}
