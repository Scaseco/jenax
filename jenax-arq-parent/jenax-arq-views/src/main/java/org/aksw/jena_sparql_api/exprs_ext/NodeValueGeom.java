package org.aksw.jena_sparql_api.exprs_ext;

import org.aksw.jena_sparql_api.views.OgcVocab;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueVisitor;
import org.postgis.Geometry;
import org.postgis.PGgeometry;

public class NodeValueGeom
    extends NodeValue
{
    private PGgeometry geometry;

    public NodeValueGeom(PGgeometry geometry) {
        this.geometry = geometry;
    }


    public PGgeometry getGeometry()
    {
        return geometry;
    }

    @Override
    protected Node makeNode() {
        RDFDatatype datatype = TypeMapper.getInstance().getSafeTypeByName(OgcVocab.wktLiteral);

        Geometry g = geometry.getGeometry();
        Node result = NodeFactory.createLiteral(g.getTypeString() + g.getValue(), datatype);

        return result;
    }

    @Override
    public void visit(NodeValueVisitor visitor) {
        throw new UnsupportedOperationException();
    }
}
