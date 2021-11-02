package org.aksw.jena_sparql_api.relationlet;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.sparql.core.Var;

public class VarRefStatic
	implements VarRef
{
	//Relationlet r;
	//protected String label;
	protected List<String> labels;
	protected Var v;
	
	public VarRefStatic(String label, Var v) {
		super();
		this.labels = Collections.singletonList(label);
		this.v = v;
	}

	public VarRefStatic(List<String> labels, Var v) {
		super();
		this.labels = labels;
		this.v = v;
	}

	public List<String> getLabels() {
		return labels;
	}

	public Var getV() {
		return v;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((labels == null) ? 0 : labels.hashCode());
		result = prime * result + ((v == null) ? 0 : v.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VarRefStatic other = (VarRefStatic) obj;
		if (labels == null) {
			if (other.labels != null)
				return false;
		} else if (!labels.equals(other.labels))
			return false;
		if (v == null) {
			if (other.v != null)
				return false;
		} else if (!v.equals(other.v))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String result = Stream.concat(labels.stream(), Stream.of(v)).map(Object::toString).collect(Collectors.joining("."));
		return result;
	}
}