package org.aksw.jenax.arq.decisiontree.api;

import java.io.Serializable;
import java.util.Objects;

public class LeafNodeImpl<C, V, T>
	extends DtNodeBase<C, V, T>
	implements LeafNode<C, V, T>, Serializable
{
	private static final long serialVersionUID = 2175371135954988521L;

	protected T value;
	
	/* For deserialization */
//	public LeafNodeImpl() {
//		super(null, null);
//	}

	
	public LeafNodeImpl(InnerNode<C, V, T> parent, V reachingValue) {
		super(parent, reachingValue);
	}

	@Override
	public T getValue() {
		return value;
	}
	
	
	@Override
	public DtNode<C, V, T> setValue(T value) {
		this.value = value;
		return this;
	}
	
	@Override
	public String toString() {
		return Objects.toString(value);
	}
	
}