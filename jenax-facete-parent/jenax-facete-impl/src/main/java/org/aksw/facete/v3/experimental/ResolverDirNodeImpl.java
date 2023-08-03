package org.aksw.facete.v3.experimental;

import java.util.Collection;

import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.api.path.Resolver;
import org.aksw.facete.v3.impl.FacetedQueryBuilder;
import org.aksw.jena_sparql_api.algebra.utils.VirtualPartitionedQuery;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.data_query.api.ResolverDirNode;
import org.aksw.jena_sparql_api.data_query.api.ResolverMultiNode;
import org.aksw.jena_sparql_api.data_query.api.ResolverNode;
import org.aksw.jenax.arq.connection.SparqlQueryConnectionTmp;
import org.aksw.jenax.sparql.relation.api.TernaryRelation;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.core.Transactional;

public class ResolverDirNodeImpl
    extends PathDirNode<ResolverNode, ResolverMultiNode>
    implements ResolverDirNode
{
    protected Resolver resolver;
    protected DataQuery<?> conn;


    public ResolverDirNodeImpl(ResolverNodeImpl parent, boolean isFwd, DataQuery<?> conn) {
        super(parent, isFwd);
        this.resolver = parent.getResolver();
        this.isFwd = isFwd;
        this.conn = conn;
    }

    public Resolver getResolver() {
        return resolver;
    }

    public Collection<TernaryRelation> getContrib() {
        Collection<TernaryRelation> result = resolver.getRdfGraphSpec(isFwd);
        return result;
    }

    @Override
    protected ResolverMultiNodeImpl viaImpl(Resource property, Node component) {
        if (component != null) {
            throw new UnsupportedOperationException("Support for referencing components not implemented");
        }

        return new ResolverMultiNodeImpl(this, property, conn);
    }

//	public Collection<TernaryRelation> getVirtualGraph() {
//		resolver.getContrib(isFwd);
//	}

    public Query rewrite(Query query) {
        Collection<TernaryRelation> views = getContrib();

        Query result = VirtualPartitionedQuery.rewrite(views, query);


        return result;
    }

    public SparqlQueryConnection virtualConn() {
        SparqlQueryConnection result = new SparqlQueryConnectionTmp() {
            @Override
            public Transactional getDelegate() {
                return null;
            }

            @Override
            public void close() {
                // No-op - its the user's responsibility
                // closing the underlying connection
            }

            // TODO The transaction management facilities should probably delegate to the underlying conn

            @Override
            public QueryExecution query(Query query) {
                System.out.println("Got query: "  + query);
                SparqlQueryConnection c = conn.connection();

                Query rewritten = rewrite(query);
                System.out.println("Rewritten query: " + rewritten);
                QueryExecution result = c.query(rewritten);

                return result;
            }
        };

        return result;
    }

    public FacetedQuery toFacetedQuery() {
        SparqlQueryConnection virtualConn = virtualConn();

        FacetedQuery result = FacetedQueryBuilder.builder()
            .configDataConnection()
                .setSource(virtualConn)
                .end()
            .create();

//		Entry<Node, Query> e = conn.toConstructQuery();
//		UnaryRelation r = new Concept(e.getValue().getQueryPattern(), (Var)e.getKey());
//
//		result.baseConcept(r);

        return result;
    }
}
