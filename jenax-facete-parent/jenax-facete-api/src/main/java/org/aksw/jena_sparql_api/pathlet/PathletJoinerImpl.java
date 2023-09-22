package org.aksw.jena_sparql_api.pathlet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.facete.v3.api.path.Resolver;
import org.aksw.facete.v3.api.path.StepImpl;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.relationlet.Relationlet;
import org.aksw.jena_sparql_api.relationlet.RelationletBinary;
import org.aksw.jena_sparql_api.relationlet.RelationletEntry;
import org.aksw.jena_sparql_api.relationlet.RelationletJoinerImpl;
import org.aksw.jena_sparql_api.relationlet.VarRef;
import org.aksw.jena_sparql_api.relationlet.VarRefStatic;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.relation.api.BinaryRelation;
import com.google.common.collect.Iterables;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementOptional;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class PathletJoinerImpl
    extends RelationletJoinerImpl<Pathlet>
    implements PathletContainer
{
    public static final Var srcJoinVar = Var.alloc("srcJoinVar");
    public static final Var tgtJoinVar = Var.alloc("tgtJoinVar");

    public static final Pathlet emptyPathlet = newPathlet(BinaryRelationImpl.empty(Vars.s));

    public static Pathlet newPathlet(BinaryRelation br) {
        return new PathletSimple(
                br.getSourceVar(), br.getTargetVar(),
                new RelationletBinary(br));
    }

//	protected Table<MemberKey, String, PathletMember> keyToAliasToMember;
    protected Table<Object, String, RelationletEntry<? extends Pathlet>> keyToAliasToMember = HashBasedTable.create();


    //protected PathletContainer container;
    protected Function<Element, Element> elementPostProcessor;

    // All element-creating methods connect to this variable
    // protected Var connectorVar;
    // sourceVar / tgtVar...?

    protected Resolver resolver;

    public PathletJoinerImpl(Resolver resolver) {
        this(resolver, emptyPathlet, RelationletJoinerImpl::flatten);
    }

    public PathletJoinerImpl() {
        this(null, emptyPathlet, RelationletJoinerImpl::flatten);
    }

//	public PathletContainer(Function<? super ElementGroup, ? extends Element> postProcessor) {
//		super(postProcessor);
//
//		// set up the root member
//		// Note super.add is necessary in order avoid setting up join of the root element with itself
//		super.add("root", new PathletSimple(
//			Vars.s, Vars.s,
//			new RelationletBinary(BinaryRelationImpl.empty(Vars.s))));
//
//		expose(srcJoinVar.getName(), "root", "s");
//		expose(tgtJoinVar.getName(), "root", "s");
//	}
    public PathletJoinerImpl(Resolver resolver, Pathlet rootPathlet, Function<? super ElementGroup, ? extends Element> postProcessor) {
        super(postProcessor);

        // set up the root member
        // Note super.add is necessary in order avoid setting up join of the root element with itself
        super.add("root", rootPathlet);
        this.resolver = resolver;
        expose(srcJoinVar.getName(), "root", rootPathlet.getSrcVar().getName());
        expose(tgtJoinVar.getName(), "root", rootPathlet.getTgtVar().getName());
    }

    public RelationletEntry<? extends Pathlet> getRootMember() {
        RelationletEntry<? extends Pathlet> result = this.getMemberByLabel("root");
        return result;
    }

    public Supplier<VarRefStatic> resolveStep(StepImpl step) {
        Path path = Path.newPath().appendStep(step);
        Supplier<VarRefStatic> result = resolvePath(path);
        return result;
    }

    public Supplier<VarRefStatic> resolvePath(Path path) {
        resolvePath(path, true);

        Supplier<VarRefStatic> result = () -> pathToVarRef(path);

        //PathletContainerImpl result = resolvePath(path, true);
        return result;
    }

    VarRefStatic pathToVarRef(Path path) {
        List<RelationletEntry<PathletJoinerImpl>> list = resolvePath(path, false);
        List<String> labels = list.stream()
                .map(RelationletEntry::getId)
                .collect(Collectors.toList());
        Var v = list.isEmpty() ? this.getTgtVar() : Iterables.getLast(list).getRelationlet().getTgtVar();

        VarRefStatic result = new VarRefStatic(labels, v);
        return result;
    }

    List<RelationletEntry<PathletJoinerImpl>> resolvePath(Path path, boolean createIfNotExists) {
        List<StepImpl> steps = Path.getSteps(path);

        List<RelationletEntry<PathletJoinerImpl>> result = resolve(steps.iterator(), createIfNotExists);
        return result;
    }


    RelationletEntry<PathletJoinerImpl> resolveStep(StepImpl step, boolean createIfNotExists) {
        RelationletEntry<PathletJoinerImpl> result;

        String type = step.getType();
        String alias = step.getAlias();
        Object key = step.getKey();

        switch(type) {
        case "optional":
            result = optional(alias, createIfNotExists);
            break;
        case "br":
            result = step(key, alias, createIfNotExists);
            break;
        default:
            throw new RuntimeException("Unknown step type " + type);
        }
        //optional()

        return result;
    }



    List<RelationletEntry<PathletJoinerImpl>> resolve(Iterator<StepImpl> it, boolean createIfNotExists) {
        List<RelationletEntry<PathletJoinerImpl>> result = new ArrayList<>();

        PathletJoinerImpl state = this;
        while(it.hasNext() && state != null) {
            StepImpl step = it.next();
            RelationletEntry<PathletJoinerImpl> tmp = state.resolveStep(step, createIfNotExists);
            state = tmp.getRelationlet();
            if(createIfNotExists) {
                Objects.requireNonNull(state, "Step resolution unexpectedly returned null");
            }

            result.add(tmp);
        }
//		} else {
//			result = this;
//		}
//
        return result;
    }

//	RelationletEntry add(Pathlet pathlet) {
//		return super.add(pathlet);
//	}

//	Pathlet getMember(Object key, String alias) {
//		Pathlet member = keyToAliasToMember.get(key, alias);
//		//PathletContainer result = member.
//		return member;
//	}

    Pathlet add(Object key, String alias, Pathlet pathlet) {
        return null;
    }

//	public static Node toNode(Object o) {
//		Node result = o instanceof Node
//			? (Node)o
//			: o instanceof RDFNode
//				? ((RDFNode)o).asNode()
//				:null;
//
//		return result;
//	}

//	public PathletContainerImpl step(Object key, String alias) {
//		PathletContainerImpl result = step(true, key, alias);
//		return result;
//	}

//	public PathletContainerImpl bwd(Object key, String alias) {
//		PathletContainerImpl result = step(false, key, alias);
//		return result;
//	}

    public RelationletEntry<PathletJoinerImpl> step(Object key, String alias, boolean createIfNotExists) {
        P_Path0 p = key instanceof P_Path0 ? (P_Path0)key : null; // Node(key);
        BinaryRelation br;
        Set<Var> pinnedVars = Collections.emptySet();
        Resolver subResolver = null;
        if(p == null) {
            br = (BinaryRelation)key;
            subResolver = null;
        } else {
            if(resolver != null) {
                subResolver = resolver.resolve(p, alias);
                Collection<RelationletBinary> brs = subResolver.getReachingRelationlet();
//				System.out.println("CONTRIBS:" + brs);
                RelationletBinary rb = brs.iterator().next();
                br = rb.getBinaryRelation();
                pinnedVars = rb.getPinnedVars();
            } else {
                Node n = p.getNode();
                boolean isFwd = p.isForward();
                br = BinaryRelationImpl.create(Vars.s, n, Vars.o, isFwd);
            }
            // TODO Union the relations using (move the method)
            ///VirtualPartitionedquery.union()

        }

        RelationletEntry<PathletJoinerImpl> result = step(createIfNotExists, subResolver, key, br, pinnedVars, alias, RelationletJoinerImpl::flatten);
        return result;

//		//BinaryRelation br = RelationUtils.createRelation(p, false, null)
//		return fwd(null, br, alias);
    }

    @Override
    public RelationletEntry<Pathlet> add(String label, Pathlet item) {
        RelationletEntry<Pathlet> result = super.add(label, item);

        // Join the added pathlet with this container's root
        RelationletEntry<? extends Pathlet> root = getRootMember();
        VarRef rootVarRef = root.createVarRef(x -> x.getTgtVar());
        VarRef memberVarRef = result.createVarRef(x -> x.getSrcVar());
        this.addJoin(rootVarRef, memberVarRef);

        return result;
    }


//	public PathletContainerImpl step(P_Path0 step, String alias) {
//		Resolver subResolver = resolver.resolve(step, alias);
//
//		Collection<BinaryRelation> brs = subResolver.getPaths();
//		// TODO Union the relations using (move the method)
//		///VirtualPartitionedquery.union()
//
//		BinaryRelation br = brs.iterator().next();
//		Object key = br;
//		PathletContainerImpl result = step(subResolver, true, key, br, alias, RelationletJoinImpl::flatten);
//		return result;
//	}

//	public PathletContainerImpl step(Object key, BinaryRelation br, String alias) {
//		PathletContainerImpl result = step(true, key, br, alias, RelationletJoinImpl::flatten);
//		return result;
//	}

    public RelationletEntry<PathletJoinerImpl> step(boolean createIfNotExists, Resolver subResolver, Object key, BinaryRelation br, Collection<Var> pinnedVars, String alias, Function<? super ElementGroup, ? extends Element> fn) {
        alias = alias == null ? "default" : alias;

        key = key == null ? "" + br : key;

        //BinaryRelation br = RelationUtils.createRelation(p, false, null);
        // Check if there is a member with this relation pattern already
        RelationletEntry<PathletJoinerImpl> result;


        result = (RelationletEntry<PathletJoinerImpl>)keyToAliasToMember.get(key, alias);//members.find(m -> m.getPattern().equals(br.getElement()));


        if(result == null && createIfNotExists) {
            Pathlet childRootPathlet = newPathlet(br);
            childRootPathlet.pinAllVars(pinnedVars);
            PathletJoinerImpl subContainer = new PathletJoinerImpl(subResolver, childRootPathlet, fn);
//			RelationletBinary r = new RelationletBinary(br);
//			//PathletMember childContainerMember = new PathletMember(childContainer, r, r.getSrcVar(), r.getTgtVar());
//
//
//			this.add(br, alias, childContainer);

            //result.add(new PathletSimple(br.getSourceVar(), br.getTargetVar(), new RelationletBinary(br)));
            // Set up a join of this node with the newly created member
            //childContainerMember.
//			result.add("root", new PathletSimple(
//					br.getSourceVar(), br.getTargetVar(),
//					new RelationletBinary(br)));
//			result.expose("joinSrc", "root", "s");
//			result.expose("joinTgt", "root", "o");

            result = this.add(subContainer);
            keyToAliasToMember.put(key, alias, result);
            //String el = e.getId();//this.getLabelForId(e.getId());
//			VarRef vr = e.createVarRef(Vars.s);


            // Join this pathlet's joinTgt with the joinSrc of the member
//			VarRef parentVarRef = getRootMember().createVarRef(x -> x.getTgtVar());
//			VarRef childVarRef = e.createVarRef(x -> x.getSrcVar());
//
//			//this.addJoin("root", Arrays.asList(Var.alloc("o")), el, Arrays.asList(Var.alloc("s")));
//			this.addJoin(parentVarRef, childVarRef);
////			this.addJoin(new VarRefStatic("root", Var.alloc("joinTgt")), new VarRefStatic(Arrays.asList(el, "root"), Vars.s));


        }

        return result;
    }

    //@Override
    public RelationletEntry<PathletJoinerImpl> optional(String label, boolean isLookup) {

        RelationletEntry<PathletJoinerImpl> result = step(isLookup, resolver, "optional", BinaryRelationImpl.empty(), Collections.emptySet(), "default",
                x -> new ElementOptional(RelationletJoinerImpl.flatten(x)));
        return result;


//		label = label == null ? "default" : label;
//
//		PathletContainer result = (PathletContainer)this.getMember("optional", label);
//
//		// Check the container for an optional member with the given label
//		// Create it if it does not exist yet.
//		if(result == null) {
//			// Return a new relationlet that wraps its effective pattern in an optional block
//			result = new PathletContainer(ElementOptional::new);
//
//			BinaryRelation br = new BinaryRelationImpl(new ElementGroup(), Vars.s, Vars.o);
//
//			result.add("root", new PathletSimple(
//					br.getSourceVar(), br.getTargetVar(),
//					new RelationletBinary(br)));
//			result.expose("joinSrc", "root", "s");
//			result.expose("joinTgt", "root", "o");
//
//			keyToAliasToMember.put("optional", label, result);
//			RelationletEntry<Pathlet> e = this.add(result);
//			String el = e.getId();//this.getLabelForId(e.getId());
//
//
//			// Join this pathlet's joinTgt with the joinSrc of the member
//			this.addJoin("root", Arrays.asList(Var.alloc("joinTgt")), el, Arrays.asList(Var.alloc("s")));
//
//			//RelationletEntry y;
//
//			//this.addJoin(lhsAlias, lhsVars, rhsAlias, rhsVars);
//			//this.addJoin("primary", "srcVar", x, result.getSrcVar());
//		}
//
//		return result;
    }


//	@Override
//	public Relationlet getMember(String alias) {
//		// TODO Auto-generated method stub
//		return null;
//	}



    @Override
    public Collection<Var> getExposedVars() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Var> getPinnedVars() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Relationlet setPinnedVar(Var var, boolean onOrOff) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Var getSrcVar() {
        return srcJoinVar;
    }


    @Override
    public Var getTgtVar() {
        return tgtJoinVar;
    }


//	@Override
//	public Pathlet optional(Pathlet rhs, boolean createIfNotExists) {
//		// TODO Auto-generated method stub
//		return null;
//	}

    @Override
    public String toString() {
        return "PathletContainer [keyToAliasToMember=" + keyToAliasToMember + "]";
    }


//	@Override
//	public PathletContainer optional(String label) {
//		optional(member, label)
//		// TODO Auto-generated method stub
//		return null;
//	}

}