package org.aksw.facete.v3.api;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.apache.jena.sparql.path.P_Path0;

/**
 * An aliased path is a sequence of step-alias pairs.
 * The alias may be null, in which case processors should internally assume a default value.
 * 
 * @author raven
 *
 */
public class AliasedPathImpl
	extends PathListBase<AliasedPath, Entry<P_Path0, String>>
	implements AliasedPath
{
	public AliasedPathImpl(List<Entry<P_Path0, String>> steps) {
		super(steps);
	}

	@Override
	protected AliasedPathImpl create(List<Entry<P_Path0, String>> steps) {
		return new AliasedPathImpl(steps);
	}
	
	public static AliasedPath empty() {
		return new AliasedPathImpl(Collections.emptyList());
	}
}
