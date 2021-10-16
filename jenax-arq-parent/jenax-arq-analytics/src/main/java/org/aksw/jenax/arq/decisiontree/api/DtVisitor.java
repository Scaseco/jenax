package org.aksw.jenax.arq.decisiontree.api;

public interface DtVisitor<C, V, T> {
	<X> X visit(InnerNode<C, V, T> innerNode);
	<X> X visit(LeafNode<C, V, T> leafNode);
}
