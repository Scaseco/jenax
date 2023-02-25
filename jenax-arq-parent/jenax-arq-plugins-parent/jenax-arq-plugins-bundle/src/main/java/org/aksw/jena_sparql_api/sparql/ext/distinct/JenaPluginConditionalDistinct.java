package org.aksw.jena_sparql_api.sparql.ext.distinct;

import java.awt.font.OpenType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.commons.util.obj.ObjectUtils;
import org.aksw.jena_sparql_api.algebra.utils.OpServiceUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;

public class JenaPluginConditionalDistinct {
    // TODO Should have a namespace
    public static final String SERVICE_IRI = "distinct:";

    // These do not need nampespaces because they appear within a specially named service.
    public static final String CONDITION = "if:";
    public static final String PATTERN = "over:";

    public static void register(ServiceExecutorRegistry registry) {
        registry.addSingleLink((opExecute, opOriginal, binding, execCxt, chain) -> {
            QueryIterator r;
            if (SERVICE_IRI.equals(OpServiceUtils.getIriOrNull(opExecute))) {

                Multimap<String, Op> args = OpServiceUtils.extractServiceArgs(opExecute);

                // condition ops -> cops
                List<ExprList> conditions = new ArrayList<>(args.size());
                Collection<Op> cops = args.get(CONDITION);
                for (Op cop : cops) {
                    OpFilter f = ObjectUtils.castAsOrNull(OpFilter.class, cop);
                    if (f != null) {
                        ExprList el = f.getExprs();
                        conditions.add(el);
                    } else if (false){
                        // Allow unit table
                    } else {
                        throw new IllegalArgumentException("Only filters allowed in condition clause, instead got: " + cop);
                    }

                    boolean isUnitTable = ObjectUtils.tryCastAs(OpTable.class, f.getSubOp())
                        .map(t -> t.isJoinIdentity()).orElse(false);
                    Preconditions.checkArgument(isUnitTable, "Filters must only have unit tables as sub ops");

                }
                // If there are no conditions then just do conventional distinct
                if (conditions.isEmpty()) {
                    conditions.add(new ExprList(NodeValue.TRUE));
                }

                Collection<Op> pattern = args.get(PATTERN);
                Preconditions.checkArgument(pattern.size() == 1, "Exactly 1 <pattern:> element expected in " + SERVICE_IRI + " - got: " + pattern);
                Op subOp = pattern.iterator().next();

                r = QC.execute(subOp, binding, execCxt);
                // r = chain.createExecution(subOp, opOriginal, binding, execCxt);
                r = new QueryIterDistinctConditional(r, null, execCxt, conditions);
            } else {
                r = chain.createExecution(opExecute, opOriginal, binding, execCxt);
            }
            return r;
        });
    }
}
