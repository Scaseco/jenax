package org.aksw.jena_sparql_api.relationlet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.cluster.IndirectEquiMap;
import org.aksw.commons.collections.collectors.CollectorUtils;
import org.aksw.commons.collections.generator.Generator;
import org.aksw.commons.collections.generator.GeneratorBlacklist;
import org.aksw.commons.collections.generator.GeneratorLendingImpl;
import org.aksw.commons.collections.stacks.NestedStack;
import org.aksw.facete.v3.api.path.Join;
import org.aksw.facete.v3.api.path.NestedVarMap;
import org.aksw.facete.v3.api.path.NestedVarMapImpl;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.VarGeneratorImpl2;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Table;

/**
 *
 *
 * We need to take care to distinguish between relationlets and relationlet entries:
 * The some relationlet object can appear in a tree under different paths of aliases.
 * (With Jena, we previously copied algebraic expressions -- Op trees --
 *  in order to ensure no two sub-expressions are equal by reference)
 *
 * In our case, it is the RelationletEntry objects that are unique.
 *
 *
 * There can be a VarRef to an PathletEntrys' targetVar - but not to the target
 *
 *
 *
 * @author raven
 *
 * @param <T>
 */
public class RelationletJoinerImpl<T extends Relationlet>
    extends RelationletBaseWithMutableFixedVars
{

    // TODO better use a TreeMap instead of a separate ridOrder list
    // Then again, with the tree map, we'd have to reassign ids if the order was changed
    //Map<Integer, RelationletEntry<T>> ridToEntry = new HashMap<>();
    //List<Integer> ridOrder = new ArrayList<>();

    protected Map<String, RelationletEntry<? extends T>> labelToRe = new LinkedHashMap<>();


    // Post processor invoked upon materialization of the join members
    protected Function<? super ElementGroup, ? extends Element> postProcessor;

    public static Stream<Element> flatten(Stream<Element> stream) {
        Stream<Element> result = stream.flatMap(e -> e instanceof ElementGroup
                ? flatten(((ElementGroup)e).getElements().stream())
                : Stream.of(e));
        return result;
    }

    public static Element flatten(Element e) {
        List<Element> tmp = flatten(Stream.of(e)).collect(Collectors.toList());
        Element result = ElementUtils.groupIfNeeded(tmp);
        return result;
    }

    public RelationletJoinerImpl() {
        this(RelationletJoinerImpl::flatten);
    }

    public RelationletJoinerImpl(Function<? super ElementGroup, ? extends Element> postProcessor) {
        super();
        this.postProcessor = postProcessor;
    }



    //List<RelationletEntry> relationletEntries = new ArrayList<>();
    // TODO In any case, labelToRid should be changed to labelToRe (i.e. make it a reference to the entry object)
    //BiMap<String, Integer> labelToRid = HashBiMap.create();//new LinkedHashMap<>();


    public Function<? super ElementGroup, ? extends Element> getPostProcessor() {
        return postProcessor;
    }

    public void setMaterializeElementPostProcessor(
            Function<? super ElementGroup, ? extends Element> postProcessor) {
        this.postProcessor = postProcessor;
    }



    // Exposed vars are seemingly variables of this relationlet and can be accessed without an alias
    // TODO Implement: Variables that are unique to members are implicitly exposed if the exposeUniqueVars flag is true
    // Expose does not imply that the final variable name is fixed.
    Map<Var, VarRefStatic> explicitExposedVars = new LinkedHashMap<>();


    //Map<String, RelationletEntry> labelToMember = new LinkedHashMap<>();

    Generator<String> gen = GeneratorLendingImpl.createPrefixedInt("x", 0);

    /**
     * Filter expressions such as for theta-joins
     * TODO Not yet supported
     *
     */
    protected List<Expr> exprs;

    // Joins
    List<Join> joins = new ArrayList<>();

//	public String getLabelForId(int id) {
//		return labelToRid.inverse().get(id);
//	}

    public String getLabelForRelationlet(Object obj) {
        String result = labelToRe.entrySet().stream()
                .filter(e -> e.getValue() == obj)
                .map(Entry::getKey)
                .findFirst().orElse(null);

        return result;
    }

    public void expose(String exposedName, String alias, String varName) {
        Var exposedVar = Var.alloc(exposedName);

        VarRefStatic varRef = new VarRefStatic(alias, Var.alloc(varName));
        explicitExposedVars.put(exposedVar, varRef);
    }


    // Allocate a new id
//	public RelationletEntry<T> add(T item) {
//		String label = "genid" + gen.next();
//		RelationletEntry<T> result = add(label, item);
//		return result;
//	}

    public <U extends T> RelationletEntry<U> add(U item) {
        String label = "genid" + gen.next();
        RelationletEntry<U> result = add(label, item);
        return result;
    }

    public <U extends T> RelationletEntry<U> add(String label, U item) {
//		String id = gen.next();
//		labelToRe.put(id, item);

        RelationletEntry<U> entry = new RelationletEntry<>(label, item);
        //labelToMember.put(label, entry);
//		ridToEntry.put(id, entry);
//		ridOrder.add(id);
//
//		if(label != null) {
//			Integer tmp = labelToRid.get(label);
//			if(tmp != null) {
//				throw new RuntimeException("Label " + label + " already in use");
//			}
//
//			labelToRid.put(label, id);
//		}

        labelToRe.put(label, entry);

        return entry;
    }

    //Set<VarRef>
    /**
     * Yield the set of variables that at least two members have in commen
     * (TODO add condition: and are not being joined on?!)
     *
     */
    public void getConflictingVars() {

    }

    /**
     * Yield the set of variables that are unqiue to a single member, hence
     * a request to it is unambiguous.
     *
     */
    public void getNonConflictingVars() {

    }

    // TODO It might be usefule for a relationlet to communicate forbidden var names
    // or suggest a generator for allowed vars. For example, certain prefixes may be forbidden as var names.
//	public Set<Var> getForbiddenVars() {
//
//	}


    public List<String> find(RelationletEntry<?> entry) {
        NestedStack<String> stack = find(entry, new RelationletEntry<>(null, this), null);
        List<String> result = stack.asList();
        return result;
    }

    /**
     * Recursively search for a given relationlet entry and return
     * its path of aliases if it exists - null otherwise
     *
     *
     * @param o
     * @return
     */
    public static NestedStack<String> find(RelationletEntry<?> entry, RelationletEntry<?> current, NestedStack<String> stack) {
        NestedStack<String> result = null;

        if(entry == current) {
            result = stack;
        } else {
            Relationlet r = current.getRelationlet();
            if(r instanceof RelationletJoinerImpl) {
                RelationletJoinerImpl<?> tmp = (RelationletJoinerImpl<?>)r;
                for(RelationletEntry<?> e : tmp.labelToRe.values()) { //tmp.getRelationletEntries()) {
                    String id = e.getId();
                    NestedStack<String> next = new NestedStack<>(stack, id);
                    result = find(entry, e, next);
                    if(result != null) {
                        break;
                    }
                }
            }

        }

        return result;
    }


    public VarRefStatic matVarRef(Object varRef) {
        VarRefStatic result;
        if(varRef instanceof VarRefStatic) {
            result = (VarRefStatic)varRef;
        } else if(varRef instanceof VarRefEntry) {
            VarRefEntry vre = (VarRefEntry)varRef;
            RelationletEntry<?> e = vre.getEntry();
            List<String> aliases = find(e);

            result = new VarRefStatic(aliases, vre.getVar());
        } else {
            throw new IllegalArgumentException("Unknown var ref type " + varRef);
        }

        return result;
    }

//	public VarRefStatic matVarRefOld(Object varRef) {
//
//		// TODO We way want to use a tag interface for var-refs + possibly visitor pattern here
//		//Entry<RelationletEntry<T>, Var> result;
//		VarRefStatic result;
//
//		if(varRef instanceof VarRefStatic) {
//			VarRefStatic vr = (VarRefStatic)varRef;
//			Var v = vr.getV();
//			List<String> labels = vr.getLabels();
//			if(labels.isEmpty()) {
//				throw new RuntimeException("Should not happen");
//			} else {
//				String label = labels.iterator().next();
//				List<String> subLabels = labels.subList(1, labels.size() - 1);
////				RelationletEntry<T> entry = ridToEntry.get(labelToRid.get(label));
////				Relationlet r = entry.getRelationlet();
////				Relationlet r = labelToRe.get(label);
//				RelationletEntry<T> entry = labelToRe.get(label);
//				Relationlet r = entry.getRelationlet();
//
//				if(subLabels.isEmpty()) {
//					result = Maps.immutableEntry(entry, v);
//				} else if(r instanceof RelationletJoinImpl) {
//					result = ((RelationletJoinImpl)r).resolveVarRef(new VarRefStatic(subLabels, v));
//				} else {
//					throw new RuntimeException("Could not reslove " + varRef);
//				}
//			}
//
//		} else if(varRef instanceof VarRefFn) {
//
//
//			VarRefFn vr = (VarRefFn)varRef;
//			result = Maps.immutableEntry(vr.getEntry(), vr.getVar());
//		} else {
//			throw new IllegalArgumentException("Unsupported var ref type: " + varRef);
//		}
//
//		return result;
//	}
//

    public static List<VarRef> toVarRefs(String alias, List<Var> vars) {
        List<VarRef> result = vars.stream().map(v -> (VarRef)new VarRefStatic(alias, v)).collect(Collectors.toList());
        return result;
    }

    public void addJoin(VarRef lhsVarRef, VarRef rhsVarRef) {
        Join join = new Join(Collections.singletonList(lhsVarRef), Collections.singletonList(rhsVarRef));
        joins.add(join);
    }

    public void addJoin(String lhsAlias, List<Var> lhsVars, String rhsAlias, List<Var> rhsVars) {
        //labelToMember.get(lhsAlias);
        List<VarRef> lhs = toVarRefs(lhsAlias, lhsVars);
        List<VarRef> rhs = toVarRefs(rhsAlias, rhsVars);

        Join join = new Join(lhs, rhs);
        joins.add(join);
    }

    public Relation effective() {
        return null;
    }

    public RelationletEntry<? extends T> getMemberByLabel(String label) {
        //return ridToEntry.get(labelToRid.get(label)).getRelationlet();
        return labelToRe.get(label);
    }

//	public Iterable<RelationletEntry<T>> getRelationletEntries() {
//		//return () -> ridOrder.stream().map(ridToEntry::get).iterator();
//		return labelToRe.values();
//	}

    public static VarRefStatic resolveMat(Map<String, RelationletSimple> map, VarRefStatic varRef) {
        Entry<String, Var> e = resolveMatCore(map, varRef);
        VarRefStatic result = new VarRefStatic(e.getKey(), e.getValue());
        return result;
    }

    /**
     * Resolve a variable against a map of materialized relationlets
     *
     * @param map
     * @param varRef
     * @return
     */
    public static Entry<String, Var> resolveMatCore(Map<String, RelationletSimple> map, VarRefStatic varRef) {
        Var v = Objects.requireNonNull(varRef.getV());
        List<String> labels = varRef.getLabels();
        if(labels.isEmpty()) {
            throw new RuntimeException("Should not happen");
        }

        String label = labels.get(0);
        List<String> subLabels = labels.subList(1, labels.size());
        Relationlet r = map.get(label);
        NestedVarMap subMap = r.getNestedVarMap();
        NestedVarMap tmp = subLabels.isEmpty() ? subMap : subMap.get(subLabels);
        Map<Var, Var> varMap = tmp.getLocalToFinalVarMap();

        Var resultVar = varMap.get(v);
        Objects.requireNonNull(resultVar);

        Entry<String, Var> result = Maps.immutableEntry(label, resultVar);

        return result;
    }
//
//	public Entry<String, Var> resolve2(VarRefStatic vr, Map<String, RelationletNested> map) {
//		return null;
//	}

//	public Entry<RelationletEntry<T>, Var> flattenRef(Map<String, RelationletNested> map, )

    /**
     * Create a snapshot of any referenced relationlet
     *
     */
    @Override
    public RelationletSimple materialize() {

        // Materialize all members
        Map<String, RelationletSimple> materializedMembers = labelToRe.values().stream()
                .collect(CollectorUtils.toLinkedHashMap(
                    RelationletEntry::getId,
                    e -> e.getRelationlet().materialize()));

//		System.out.println("Processing: " + this);

//		if(this.toString().equals("PathletContainer [keyToAliasToMember={?s ?o | ?s  a                     ?o={default=PathletContainer [keyToAliasToMember={}]}, optional={default=PathletContainer [keyToAliasToMember={?s ?o | ?s  a                     ?o={default=PathletContainer [keyToAliasToMember={?s ?o | ?s  <http://www.w3.org/2000/01/rdf-schema#label>  ?o={p1=PathletContainer [keyToAliasToMember={}]}}]}}]}}]")) {
//			System.out.println("DEBUG POINT");
//		}

//		if(materializedMembers.toString().contains("http://www.example.org/test")) {
//			System.out.println("DEBUG POINT");
//		}

//		Map<String, RelationletNested> materializedMembersByLabel = materializedMembers.entrySet().stream()
//				.collect(CollectorUtils.toLinkedHashMap(
//						e -> getLabelForId(e.getKey()),
//						Entry::getValue));

        // The localToFinalVarMap is yet to be built
        //new NestedVarMap(localToFinalVarMap, fixedFinalVars)



        //Set<Var> forbiddenVars = new HashSet<>();
        Predicate<Var> baseBlacklist = x -> false;

//		List<?> members = new ArrayList<>();
//		List<Map<Var, Var>> memberVarMaps = members.stream()
//				.map(x -> new LinkedHashMap<Var, Var>())
//				.collect(Collectors.toList());


        IndirectEquiMap<Entry<String, Var>, Var> aliasedVarToEffectiveVar = new IndirectEquiMap<>();

        for(Join join : joins) {
//			RelationletEntry lhsEntry = ridToEntry.get(labelToRid.get(join.getLhsAlias()));
//			RelationletEntry rhsEntry = ridToEntry.get(labelToRid.get(join.getRhsAlias()));
//			List<Var> lhsVars = join.getLhsVars();
//			List<Var> rhsVars = join.getRhsVars();

//			int lhsId = lhsEntry.getId();
//			int rhsId = rhsEntry.getId();

//			Relationlet lhsRel = lhsEntry.getRelationlet();
//			Relationlet rhsRel = rhsEntry.getRelationlet();
//
//			Map<Var, Var> lhsVarMap;
//			Map<Var, Var> rhsVarMap;


            List<VarRef> lhsRefs = join.getLhs();
            List<VarRef> rhsRefs = join.getRhs();


            //int n = lhsVars.size();
            int n = join.getLhs().size();
            // TODO Assert that var lists sizes are equal

            for(int i = 0; i < n; ++i) {
                VarRef lhsRef = lhsRefs.get(i);
                VarRef rhsRef = rhsRefs.get(i);

//				Entry<RelationletEntry<T>, Var> lhsEntry = resolveVarRef(lhsRef);
//				Entry<RelationletEntry<T>, Var> rhsEntry = resolveVarRef(rhsRef);

                VarRefStatic rawLhsEntry = matVarRef(lhsRef);
                VarRefStatic rawRhsEntry = matVarRef(rhsRef);

                Entry<String, Var> lhsEntry = resolveMatCore(materializedMembers, rawLhsEntry);
                Entry<String, Var> rhsEntry = resolveMatCore(materializedMembers, rawRhsEntry);

//				VarRefStatic lhsEntry = resolveMat(materializedMembers, rawLhsEntry);
//				VarRefStatic rhsEntry = resolveMat(materializedMembers, rawRhsEntry);


//				String lhsId = lhsEntry.getKey().getId();
//				String rhsId = rhsEntry.getKey().getId();
//
//				Var lhsVar = lhsEntry.getValue();
//				Var rhsVar = rhsEntry.getValue();


//				Var lhsVar = lhsVars.get(i);
//				Var rhsVar = rhsVars.get(i);

//				Entry<String, Var> lhsE = Maps.immutableEntry(lhsId, lhsVar);
//				Entry<String, Var> rhsE = Maps.immutableEntry(rhsId, rhsVar);

                // Put the rhs var first, so it gets renamed first
                aliasedVarToEffectiveVar.stateEqual(rhsEntry, lhsEntry);
            }
        }


        // Now that we have clustered the join variables, allocate for each cluster an
        // effective variable
        // In order to tidy up the output, we sort clusters that only make use of the same variable first
        // If multiple clusters only make use of the same variable, they are ordered by size and var name

        Map<Integer, Collection<Entry<String, Var>>> clusters = aliasedVarToEffectiveVar.getEquivalences().asMap();

//		Map<Integer, Collection<Entry<String, Var>>> clusters = rawClusters.entrySet().stream()
//				.collect(Collectors.toMap(
//						Entry::getKey,
//						e -> e.getValue().stream()
//							.map(f -> resolveMat(materializedMembers, f))
//							.collect(Collectors.toList())
//						));

        // Index of which relationlets mention a var
        // Used to check whether a cluster covers all mentions of a var - in this case,
        // no renaming has to be performed
        Multimap<Var, String> varToRids = HashMultimap.create();
        //Multimap<Integer, Var> ridToVars = LinkedHashMultimap.create();


//		for(RelationletEntry<T> e : getRelationletEntries()) {
        for(Entry<String, RelationletSimple> ee : materializedMembers.entrySet()) {
            //int id = e.getId();
            //T r = e.getRelationlet();
            String id = ee.getKey();
            Relationlet r = ee.getValue();
            Set<Var> varsMentioned = r.getVarsMentioned();

            for(Var v : varsMentioned) {
                varToRids.put(v, id);

                //ridToVars.put(id, v);
            }
        }

        // The same var can join in different clusters with different relationlets
//		Multimap<Var, Integer> joinVarToElRids = HashMultimap.create();
//		for(Entry<Var, Integer> e : aliasedVarToEffectiveVar.getEquivalences()) {
//			joinVarToElRids.put(e.getKey(), oo);
//		}

        Map<Integer, Set<Var>> clusterToVars = clusters.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        e -> e.getValue().stream()
                            .map(Entry::getValue)
                            //.map(Var::getName)
                            .collect(Collectors.toSet())));
        //System.out.println("cluster to vars: " + clusterToVars);

        Set<Var> matVarsMentioned = materializedMembers.values().stream()
                .map(Relationlet::getVarsMentioned)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        Set<Var> takenFinalVars = new HashSet<>();

        Collection<Var> mentionedVars = matVarsMentioned; //getVarsMentioned();//Collections.emptySet();
        Predicate<Var> isBlacklisted = baseBlacklist.or(mentionedVars::contains).or(takenFinalVars::contains);

        //GeneratorLending<Var>
        Generator<Var> basegen = VarGeneratorImpl2.create();
        Generator<Var> vargen = GeneratorBlacklist.create(basegen, isBlacklisted);


        Table<String, Var, Var> ridToVarToFinalVal = HashBasedTable.create();
        for(Entry<Integer, Collection<Entry<String, Var>>> e : clusters.entrySet()) {
            int clusterId = e.getKey();
            Collection<Entry<String, Var>> members = e.getValue();

            // Check the cluster for variables marked as fix
            Set<Var> fixedVars = new LinkedHashSet<>();
            Multimap<Var, String> varToRe = ArrayListMultimap.create();
            for(Entry<String, Var> f : members) {
                String label = f.getKey();
                Relationlet r = materializedMembers.get(label);
                NestedVarMap nvm = r.getNestedVarMap();

                //RelationletEntry<T> re = f.getKey();
                //Relationlet r = re.getRelationlet();
                Var v = f.getValue();

                varToRe.put(v, label);
                boolean isFixed = nvm.isFixed(v); //r.isFixed(v);
                if(isFixed) {
                    fixedVars.add(v);
                }
            }

            if(fixedVars.size() > 1) {
                Multimap<Var, String> conflictMentions = Multimaps.filterKeys(varToRe, fixedVars::contains);
                throw new RuntimeException("Conflicting fixed vars encountered when processing join: " + fixedVars + " with mentions in " + conflictMentions);
            }

            // If there is a fixed variable within the set of used vars, pick this one
            // If there are multiple ones, we have a conflict and throw an exception
            // TODO Such conflicts should be detected early when adding the join condition!



            Var finalVar = null;
            boolean isFinalVarFixed = false;

            if(fixedVars.size() == 1) {
                finalVar = fixedVars.iterator().next();
                isFinalVarFixed = true;
            }


            if(finalVar == null) {
                // If any of the variables is not taken yet, use this for the cluster otherwise
                // allocate a fresh variable
                Set<Var> usedVars = varToRe.keySet();

                for(Var var : usedVars) {
                    if(!takenFinalVars.contains(var)) {
                        finalVar = var;
                        break;
                    }
                }
            }

            if(finalVar == null) {
                finalVar = vargen.next();
            }

            takenFinalVars.add(finalVar);

//			for(Entry<RelationletEntry<T>, Var> ridvar : clusters.get(clusterId)) {
            for(Entry<String, Var> ridvar : clusters.get(clusterId)) {
                String rid = ridvar.getKey(); //.getId();
                Var var = ridvar.getValue();
                ridToVarToFinalVal.put(rid, var, finalVar);

                // Remove the entry from the conflicts
                varToRids.remove(var, rid);
                //ridToVars.remove(rid, var);
            }
        }

        // Make all remaining variables distinct from each other
//		for(Integer rid : ridOrder) {
//			ridToVars
//		}

        for(Entry<Var, Collection<String>> e : varToRids.asMap().entrySet()) {
            Var var = e.getKey();
            Collection<String> tmpRids = e.getValue();

            // Sort the rids according to the ridOrder
            // For this purpose, get the labels in order, and then retain only those from the cluster
//			List<Integer> rids = new ArrayList<>(ridOrder);
            List<String> rids = new ArrayList<>(materializedMembers.keySet());
            rids.retainAll(tmpRids);

            for(String rid : rids) {
                //boolean isFixed = labelToRe.get(rid).getRelationlet().isFixed(var);
                boolean isFixed = materializedMembers.get(rid).getNestedVarMap().isFixed(var);
                if(isFixed) {
                    takenFinalVars.add(var);
                    ridToVarToFinalVal.put(rid, var, var);
                }
            }

            for(String rid : rids) {
                //boolean isFixed = labelToRe.get(rid).getRelationlet().isFixed(var);
                boolean isFixed = materializedMembers.get(rid).getNestedVarMap().isFixed(var);
                if(!isFixed) {
                    boolean isTaken = takenFinalVars.contains(var);

                    Var finalVar = isTaken ? vargen.next() : var;
                    takenFinalVars.add(finalVar);

                    ridToVarToFinalVal.put(rid, var, finalVar);
                }
            }
        }

//		Multimap<Var, Entry<Integer, Var>> finalVarToRidVars;


        ElementGroup group = new ElementGroup();
//		for(RelationletEntry<T> re : getRelationletEntries()) {
        for(Entry<String, RelationletSimple> ee : materializedMembers.entrySet()) {
            //int id = e.getId();
            //T r = e.getRelationlet();
            String rid = ee.getKey();
            RelationletSimple r = ee.getValue();
            Element el = r.getElement();
//			int rid = re.getId();
//			Element el = re.getRelationlet().getElement();

            Map<Var, Var> originToFinal = ridToVarToFinalVal.row(rid);
            Element contrib = ElementUtils.applyNodeTransform(el, new NodeTransformSubst(originToFinal));
            group.addElement(contrib);

        }


        Map<Var, Var> resolvedExposedVar = new LinkedHashMap<>();
        for(Entry<Var, VarRefStatic> eve : explicitExposedVars.entrySet()) {
            Var key = eve.getKey();
            VarRefStatic vr = eve.getValue();

//			materializedMembersByLabel.
//
//			String label = vr.getLabel();
//			Var refVar = vr.getV();
//			int rid = labelToRid.get(label);
//			Var finalVar = ridToVarToFinalVal.get(rid, refVar);
            Entry<String, Var> finalE = resolveMatCore(materializedMembers, vr);
            String rid = finalE.getKey();
            Var v = finalE.getValue();
            Var finalVar = ridToVarToFinalVal.get(rid, v);
            Objects.requireNonNull(finalVar);
            resolvedExposedVar.put(key, finalVar);
        }

        //ridToVarToFinalVal

        // TODO Adjust the var maps of the materialized members
        // This way, a deep reference such as a.b.c.?x can yield the effective variable at this


        Map<String, NestedVarMap> memberToNestedVarMap = new LinkedHashMap<>();
        for(Entry<String, RelationletSimple> e : materializedMembers.entrySet()) {
            String label = e.getKey();
            NestedVarMap clone = e.getValue().getNestedVarMap().clone();
            //int rid = labelToRid.get(label);
            Map<Var, Var> memberMap = ridToVarToFinalVal.row(label);

            // In-place op
            clone.transformValues(memberMap::get);

            memberToNestedVarMap.put(label, clone);
        }


        Set<Var> globalFixedVars = materializedMembers.values().stream()
                .flatMap(re -> re.getNestedVarMap().getFixedFinalVars().stream())
                .collect(Collectors.toSet());

        NestedVarMap nvm = new NestedVarMapImpl(resolvedExposedVar, globalFixedVars, memberToNestedVarMap);

        Element finalElement = postProcessor == null
                ? group
                : postProcessor.apply(group);

        RelationletSimple result = new RelationletNestedImpl(finalElement, nvm, materializedMembers);

//		System.out.println(ridToVarToFinalVal);
//		System.out.println(group);
        return result;
    }

    @Override
    public Set<Var> getVarsMentioned() {
        Set<Var> result = labelToRe.values().stream()
            .map(RelationletEntry::getRelationlet)
            .map(Relationlet::getVarsMentioned)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        return result;
    }

    /**
     * Flatten a var name by giving it a new name that is visible
     * directly at this relationlet
     *
     * @param name
     * @param alias
     * @param var
     * @return
     */
//	public Var expose(Var name, String alias, Var var) {
//
//	}

    /**
     * If var is unique among all member relationlets (up to the point of reference)
     * expose its occurrence. If the var is ambiguous, raise an exception.
     *
     * @param var
     */
//	public expose(Var var) {
//
//	}
}

