package org.aksw.jenax.arq.util.template;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.modify.TemplateLib;

/** Similar to Jena's {@link TemplateLib} but without remapping of blank nodes */
public class TemplateLib2 {
    public static Iterator<Triple> calcTriples(Triple tuple, Iterator<Binding> bindings) {
        return Iter.iter(bindings)
            .map(binding -> {
                Triple r = Substitute.substitute(tuple, binding);
                return r;
            })
            .filter(Triple::isConcrete);
    }

    public static Iterator<Triple> calcTriples(List<Triple> tuples, Iterator<Binding> bindings) {
        return Iter.iter(bindings)
            .flatMap(binding -> Iter.iter(tuples)
                .map(tuple -> {
                    Triple r = Substitute.substitute(tuple, binding);
                    return r;
                })
                .filter(Triple::isConcrete)
            );
    }

    public static Iterator<Quad> calcQuads(Quad tuple, Iterator<Binding> bindings) {
        return Iter.iter(bindings)
            .map(binding -> {
                Quad r = Substitute.substitute(tuple, binding);
                return r;
            })
            .filter(Quad::isConcrete);
    }

    public static Iterator<Quad> calcQuads(List<Quad> tuples, Iterator<Binding> bindings) {
        return Iter.iter(bindings)
            .flatMap(binding -> Iter.iter(tuples)
                    .map(tuple -> {
                        Quad r = Substitute.substitute(tuple, binding);
                        return r;
                    })
                    .filter(Quad::isConcrete)
            );
    }
}
