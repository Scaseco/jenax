package org.aksw.jena_sparql_api.rx.query_flow;

import java.util.function.Function;

import org.aksw.jenax.arq.util.binding.BindingOverMapMutable;
import org.aksw.jenax.arq.util.syntax.VarExprListUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.function.FunctionEnv;

/**
 * Execution of assignment
 *
 * Based on {@link org.apache.jena.sparql.engine.iterator.QueryIterAssign}
 * @author raven
 *
 */
public class QueryFlowAssign
//    extends QueryFlowBase<Binding>
{
//    protected VarExprList exprs;
//
//    public QueryFlowAssign(FlowableEmitter<Binding> emitter, FunctionEnv execCxt, VarExprList exprs) {
//        super(emitter, execCxt);
//        this.exprs = exprs;
//    }
//
//    @Override
//    public void onNext(@NonNull Binding binding) {
//
//        emitter.onNext(b);
//    }
//

    /**
     *
     * @implNote
     * 	 This implementation is very similar to {@link VarExprListUtils#eval(VarExprList, Binding, FunctionEnv)}.
     *   The only difference is that it throws an exception on reassignment.
     */
    public static Binding assign(Binding binding, VarExprList exprs, FunctionEnv execCxt) {
        BindingOverMapMutable mb = BindingOverMapMutable.copyOf(binding);
        for (Var v : exprs.getVars()) {
            Node n = exprs.get(v, mb, execCxt);
            if (n != null) {
                Node m = mb.get(v);
                if(m != null) {
                    if(m.sameValueAs(n)) {
                        continue;
                    } else {
                        // TODO Just return from function?
                        throw new RuntimeException("Encountered incompatible mapping in join");
                    }
                }

                mb.add(v, n);
            }
        }
        Binding result = BindingBuilder.create().addAll(mb).build();
        return result;
    }

    public static Function<Binding, Binding> createMapper(VarExprList exprs, FunctionEnv execCxt) {
        return binding -> assign(binding, exprs, execCxt);
    }

}
