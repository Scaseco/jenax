package org.aksw.jena_sparql_api.sparql.ext.xml;

import java.util.AbstractList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ListOverNodeList
	extends AbstractList<Node>
{
	protected NodeList nodeList;

	public ListOverNodeList(NodeList nodeList) {
		super();
		this.nodeList = nodeList;
	}

	@Override
	public Node get(int index) {
		return nodeList.item(index);
	}

	@Override
	public int size() {
		return nodeList.getLength();
	}

}
