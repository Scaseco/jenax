package org.aksw.facete.v3.api.path;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.collectors.CollectorUtils;
import org.apache.jena.sparql.core.Var;

public class NestedVarMapImpl
	implements NestedVarMap
{
	protected Map<Var, Var> localToFinalVarMap;
	protected Map<String, NestedVarMap> memberVarMap;
	protected Set<Var> fixedFinalVars;
	
	
	public NestedVarMapImpl(Map<Var, Var> localToFinalVarMap, Set<Var> fixedFinalVars) {
		this(localToFinalVarMap, fixedFinalVars, Collections.emptyMap());
	}

	public NestedVarMapImpl(Map<Var, Var> localToFinalVarMap, Set<Var> fixedFinalVars, Map<String, NestedVarMap> memberVarMap) {
		super();
		this.localToFinalVarMap = localToFinalVarMap;
		this.memberVarMap = memberVarMap;
		this.fixedFinalVars = fixedFinalVars;
	}
	
	@Override
	public Set<Var> getVarsMentioned() {
		Set<Var> result = Stream.concat(localToFinalVarMap.values().stream(),
				memberVarMap.values().stream().flatMap(x -> x.getVarsMentioned().stream()))
				.collect(Collectors.toSet());
		return result;				
	}

	@Override
	public NestedVarMap get(String alias) {
		NestedVarMap result = memberVarMap.get(alias);
		return result;
	}
	
	@Override
	public Set<Var> getFixedFinalVars() {
		return fixedFinalVars;
	}

	@Override
	public Map<Var, Var> getLocalToFinalVarMap() {
		return localToFinalVarMap;
	}

	@Override
	public Map<String, NestedVarMap> getMemberVarMap() {
		return memberVarMap;
	}
	
	@Override
	public void transformValues(Function<? super Var, ? extends Var> fn) {
		for(Entry<Var, Var> e : localToFinalVarMap.entrySet()) {
			Var before = e.getValue();
			Var after = fn.apply(before);
			e.setValue(after);
		}
		
		for(NestedVarMap child : memberVarMap.values()) {
			child.transformValues(fn);
		}
		
		// Update fixed vars
		// Note: Fixed vars typically should not be remapped in the first place
		Collection<Var> tmp = fixedFinalVars.stream().map(fn::apply).collect(Collectors.toList());
		
//		if(!tmp.equals(fixedFinalVars)) {
//			System.out.println("DEBUG POINT");
//		}
		fixedFinalVars.clear();
		fixedFinalVars.addAll(tmp);
		
	}
	
	public NestedVarMapImpl clone() {
		Map<Var, Var> cp1 = new LinkedHashMap<>(localToFinalVarMap);
		Set<Var> cp2 = new LinkedHashSet<>(fixedFinalVars);
		Map<String, NestedVarMap> cp3 = memberVarMap.entrySet().stream()
				.collect(CollectorUtils.toLinkedHashMap(Entry::getKey, e -> e.getValue().clone()));
		
		NestedVarMapImpl result = new NestedVarMapImpl(cp1, cp2, cp3);
		return result;
	}

	@Override
	public String toString() {
		return "NestedVarMap [localToFinalVarMap=" + localToFinalVarMap + ", memberVarMap=" + memberVarMap + "]";
	}
}