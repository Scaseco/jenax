package org.aksw.jenax.arq.util.var;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.jena.sparql.core.Var;

/**
 * A nestable partition based on a list of variables.
 */
public class VarPartition {
    protected List<Var> partitionVars;
    // protected List<SortCondition> sortConditions
    protected VarPartition subPartition;

    protected VarPartition(List<Var> partitionVars, VarPartition subPartition) {
        super();
        this.partitionVars = partitionVars;
        this.subPartition = subPartition;
    }

    public static VarPartition of(List<Var> partitionVars) {
        return of(partitionVars, null);
    }

    public static VarPartition of(List<Var> partitionVars, VarPartition subPartition) {
        return new VarPartition(new ArrayList<>(partitionVars), subPartition);
    }

    public List<Var> getPartitionVars() {
        return partitionVars;
    }

    public VarPartition getSubPartition() {
        return subPartition;
    }

    @Override
    public int hashCode() {
        return Objects.hash(partitionVars, subPartition);
    }

    @Override
    public String toString() {
        String varStr = partitionVars.stream().map(Object::toString).collect(Collectors.joining(", "));
        String result = subPartition == null
                ? "(" + varStr + ")"
                : "(" + varStr + "," + subPartition.toString() + ")";
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VarPartition other = (VarPartition) obj;
        return Objects.equals(partitionVars, other.partitionVars) && Objects.equals(subPartition, other.subPartition);
    }
}
