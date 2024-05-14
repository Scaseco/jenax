package org.aksw.jena_sparql_api.data_query.api;

import org.apache.jena.graph.NodeVisitor;
import org.apache.jena.graph.Node_Ext;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;

public class NodePath
    extends Node_Ext<SPath>
    implements PathTraitString<NodePath>
{
    public NodePath(SPath path) {
        super(path);
    }

    @Deprecated // Use get()
    public SPath getPath() {
        return get();
    }

    @Override
    public Object visitWith(NodeVisitor v) {
        throw new UnsupportedOperationException();
    }


//	@Override
//	public boolean equals(Object o) {
//		boolean result = this == o || NodePath.class.equals(o.getClass()) && Objects.equals(path, ((NodePath)o).getPath());
//		return result;
//	}

    @Override
    public NodePath step(String p, boolean isFwd) {
        SPath tmp = get().get(p, !isFwd);
        return new NodePath(tmp);
    }

    public Expr asExpr() {
        return NodeValue.makeNode(this);
    }

    @Override
    public String toString() {
        return "NodePath(" + get() + ")";
    }

    @Override
    public String toString(PrefixMapping pmap) {
        return toString();
    }
}
