package org.aksw.jenax.arq.dataset.diff;

import java.util.Map;

import org.apache.jena.sparql.core.Transactional;

public interface TransactionalMap<K, V>
    extends Map<K, V>, Transactional
{

}
