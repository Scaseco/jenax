package org.aksw.jenax.arq.aggregation;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.function.FunctionEnv;

/**
 * Aggregator for predefined keys
 *
 * @author raven
 *
 * @param <Map>
 */
public class AccObject<K>
    implements Acc<Map<K, ?>>
{
    private Map<K, Acc<?>> keyToSubAcc;

    public AccObject(Map<K, Acc<?>> keyToSubAcc) {
        this.keyToSubAcc = keyToSubAcc;
    }

    @Override
    public void accumulate(Binding binding, FunctionEnv env) {
        for(Entry<K, Acc<?>> entry : keyToSubAcc.entrySet()) {
            Acc<?> acc = entry.getValue();
            acc.accumulate(binding, env);
        }
    }

    @Override
    public Map<K, Object> getValue() {
        Map<K, Object> result = new HashMap<K, Object>();

        for(Entry<K, Acc<?>> entry : keyToSubAcc.entrySet()) {
            K k = entry.getKey();
            Acc<?> acc = entry.getValue();
            Object v = acc.getValue();
            result.put(k, v);
        }

        return result;
    }
}
