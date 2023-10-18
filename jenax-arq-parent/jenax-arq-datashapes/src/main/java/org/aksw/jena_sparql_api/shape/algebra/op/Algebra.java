package org.aksw.jena_sparql_api.shape.algebra.op;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.jena_sparql_api.shape.syntax.Element;
import org.aksw.jena_sparql_api.shape.syntax.ElementVisitor;
import org.aksw.jena_sparql_api.shape.syntax.ElementVisitorSparql;
import org.aksw.jenax.arq.util.var.VarGeneratorImpl2;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.apache.jena.sparql.core.Var;

public class Algebra {
    protected static final ElementVisitor<Op> elementCompiler = new ElementVisitorSparql();

    public static Op compile(Element e) {
        Op result = e.accept(elementCompiler);
        return result;
    }

    public static Fragment1 toConcept(Op op) {
        Generator<Var> generator = VarGeneratorImpl2.create();
        OpVisitor<Fragment1> opCompiler = new OpVisitorSparql(generator);
        Fragment1 result = op.accept(opCompiler);
        return result;
    }
}
