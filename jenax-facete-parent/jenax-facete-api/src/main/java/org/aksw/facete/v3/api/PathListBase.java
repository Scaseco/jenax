package org.aksw.facete.v3.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Iterables;

public abstract class PathListBase<T extends PathBase<T, S>, S>
	implements PathBase<T, S>
{
	protected List<S> steps;
	
	public PathListBase(List<S> steps) {
		this.steps = steps;
	}
	
	public List<S> getSteps() {
		return steps;
	}
	
	abstract T create(List<S> steps);

	@Override
	public T getParent() {
		T result = steps.isEmpty()
				? null
				: create(steps.subList(0, steps.size() - 1));
		return result;
	}

	@Override
	public S getLastStep() {
		S result = steps.isEmpty()
			? null
			: Iterables.getLast(steps);
		return result;
	}
	
	@Override
	public T subPath(S step) {
		List<S> copy = new ArrayList<>(steps);
		copy.add(step);
		T result = create(copy);
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((steps == null) ? 0 : steps.hashCode());
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
		PathListBase other = (PathListBase) obj;
		if (steps == null) {
			if (other.steps != null)
				return false;
		} else if (!steps.equals(other.steps))
			return false;
		return true;
	}
	
	public String toString() {
		return Objects.toString(steps);
	}

}
