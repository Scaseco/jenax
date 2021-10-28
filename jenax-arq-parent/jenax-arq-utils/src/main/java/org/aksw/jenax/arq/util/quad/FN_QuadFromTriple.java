package org.aksw.jenax.arq.util.quad;

import com.google.common.base.Function;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;


/** Mapper from triples to quads by means of injection of a constant graph node */
public class FN_QuadFromTriple
    implements Function<Triple, Quad>
{
    private Node g;

    public FN_QuadFromTriple(Node g) {
        super();
        this.g = g;
    }

    @Override
    public Quad apply(Triple triple) {
        Quad result = Quad.create(g, triple);
        return result;
    }

    public static FN_QuadFromTriple create(Node g) {
        FN_QuadFromTriple result = new FN_QuadFromTriple(g);
        return result;
    }

    public static final FN_QuadFromTriple fnDefaultGraphNodeGenerated = new FN_QuadFromTriple(Quad.defaultGraphNodeGenerated);
}
