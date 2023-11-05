package org.aksw.facete.v3.experimental;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.facete.v3.api.path.Resolver;
import org.aksw.facete.v3.api.path.ResolverBase;
import org.aksw.jena_sparql_api.relationlet.RelationletBinary;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.aksw.jenax.sparql.fragment.api.Fragment3;
import org.apache.jena.sparql.path.P_Path0;

public class ResolverUnion
    extends ResolverBase
{
    protected Collection<? extends Resolver> resolvers;

    public ResolverUnion(Resolver parent, Collection<? extends Resolver> resolvers) {
        super(parent);
        this.resolvers = resolvers;
    }


    @Override
    public Resolver resolve(P_Path0 step, String alias) {
        Collection<Resolver> children = resolvers.stream().map(r -> r.resolve(step, alias))
                .collect(Collectors.toList());

        Resolver result = new ResolverUnion(this, children);
        return result;
    }

    @Override
    public Collection<Fragment3> getRdfGraphSpec(boolean fwd) {
        List<Fragment3> result = resolvers.stream()
                .flatMap(resolver -> resolver.getRdfGraphSpec(fwd).stream())
                .collect(Collectors.toList());

//		List<TernaryRelation> result = new ArrayList<>();
//		for(Resolver resolver : resolvers) {
//			Collection<TernaryRelation> contribs = resolver.getContrib(fwd);
//			result.addAll(contribs);
//		}

        return result;
    }

    @Override
    public Collection<Fragment2> getPaths() {
        List<Fragment2> result = resolvers.stream()
                .flatMap(resolver -> {
                    Collection<Fragment2> tmp = resolver.getPaths();
                    return tmp.stream();
                })
                .collect(Collectors.toList());
        return result;
    }

    @Override
    public Collection<RelationletBinary> getReachingRelationlet() {
        List<RelationletBinary> result = resolvers.stream()
                .flatMap(resolver -> {
                    Collection<RelationletBinary> tmp = resolver.getReachingRelationlet();
                    return tmp.stream();
                })
                .collect(Collectors.toList());
        return result;

    }
}