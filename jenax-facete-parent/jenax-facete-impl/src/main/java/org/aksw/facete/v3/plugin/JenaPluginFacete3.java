package org.aksw.facete.v3.plugin;

import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.bgp.api.BgpMultiNode;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.facete.v3.bgp.api.XFacetedQuery;
import org.aksw.facete.v3.bgp.impl.BgpMultiNodeImpl;
import org.aksw.facete.v3.bgp.impl.BgpNodeImpl;
import org.aksw.facete.v3.bgp.impl.XFacetedQueryImpl;
import org.aksw.facete.v3.impl.FacetConstraintImpl;
import org.aksw.facete.v3.impl.FacetCountImpl;
import org.aksw.facete.v3.impl.FacetValueCountImpl;
import org.aksw.facete.v3.impl.RangeSpec;
import org.aksw.facete.v3.impl.RangeSpecImpl;
import org.aksw.jena_sparql_api.data_query.api.SPath;
import org.aksw.jena_sparql_api.data_query.impl.SPathImpl;
import org.aksw.jenax.arq.util.implementation.SimpleImplementation;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginFacete3
    implements JenaSubsystemLifecycle
{
    public void start() {
        init();
    }

    @Override
    public void stop() {
    }

    public static void init() {
        init(BuiltinPersonalities.model);
    }

    public static void init(Personality<RDFNode> p) {
        p.add(BgpNode.class, new SimpleImplementation(BgpNodeImpl::new));
        p.add(BgpMultiNode.class, new SimpleImplementation(BgpMultiNodeImpl::new));
        p.add(XFacetedQuery.class, new SimpleImplementation(XFacetedQueryImpl::new));


        p.add(FacetConstraint.class, new SimpleImplementation(FacetConstraintImpl::new));

        p.add(SPath.class, new SimpleImplementation(SPathImpl::new));
//		p.add(Dimension.class, new SimpleImplementation(DimensionImpl::new));


//    	p.add(MapState.class, new SimpleImplementation(MapStateImpl::new));
//    	p.add(Viewport.class, new SimpleImplementation(ViewportImpl::new));


        p.add(FacetCount.class, new SimpleImplementation(FacetCountImpl::new));
        p.add(FacetValueCount.class, new SimpleImplementation(FacetValueCountImpl::new));


        // TODO Make an interface

        p.add(RangeSpec.class, new SimpleImplementation(RangeSpecImpl::new));


    }
}
