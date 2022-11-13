package org.aksw.jena_sparql_api.rx.query_flow;

import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.commons.util.stream.StreamFunction;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.modify.TemplateLib;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.NodeFactoryExtra;

import com.google.common.base.Preconditions;

// FIXME Move out of rx package
public class QueryStreamOps {

    /** Create a mapper for a construct query yielding triples (similar to tarql) */
    public static StreamFunction<Binding, Triple> createMapperTriples(Query query) {
        Preconditions.checkArgument(!query.isConstructType(), "Construct query expected");

        Template template = query.getConstructTemplate();
        Op op = Algebra.compile(query);

        return upstream ->
	    	StreamFunction.identity(Binding.class)
	    		.andThen(createMapperBindings(op))
	    		.andThenFlatMap(createMapperTriples(template)::apply)
	    		.apply(upstream);
    }

    /** Create a mapper for a construct query yielding quads (similar to tarql) */
    public static StreamFunction<Binding, Quad> createMapperQuads(Query query) {
        Preconditions.checkArgument(query.isConstructType(), "Construct query expected");

        Template template = query.getConstructTemplate();
        Op op = Algebra.compile(query);

        return upstream ->
        	StreamFunction.identity(Binding.class)
        		.andThen(createMapperBindings(op))
        		.andThenFlatMap(createMapperQuads(template)::apply)
        		.apply(upstream);
    }


    public static StreamFunction<Binding, Binding> createMapperBindings(Op op) {
        return upstream -> {
            DatasetGraph ds = DatasetGraphFactory.empty();
            Context cxt = ARQ.getContext().copy();
            ExecutionContext execCxt = new ExecutionContext(cxt, ds.getDefaultGraph(), ds, QC.getFactory(cxt));
            return upstream.flatMap(binding -> Iter.asStream(QC.execute(op, binding, execCxt)));
        };
    }


    public static Function<Binding, Stream<Triple>> createMapperTriples(Template template) {
        return binding -> Iter.asStream(TemplateLib.calcTriples(template.getTriples(), Collections.singleton(binding).iterator()));
    }

    /**
     *
     * Usage
     *   Flowable<Quad> quads = flowOfBindings.concatMap(createMapperQuads(template)::apply);
     *
     *
     * @param template
     * @return
     */
    public static Function<Binding, Stream<Quad>> createMapperQuads(Template template) {
        return binding -> Iter.asStream(TemplateLib.calcQuads(template.getQuads(), Collections.singleton(binding).iterator()));
    }

    /**
     * Utility method to set up a default execution context
     *
     * @return
     */
    public static ExecutionContext createExecutionContextDefault() {
        Context context = ARQ.getContext().copy();
        context.set(ARQConstants.sysCurrentTime, NodeFactoryExtra.nowAsDateTime());
        ExecutionContext result = new ExecutionContext(context, null, null, null);
        return result;
    }


}
