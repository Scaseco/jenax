package org.aksw.jenax.arq.dataset.diff;

import java.util.Collection;

import org.apache.jena.sparql.core.Transactional;

public interface TransactionalCollection<T>
    extends Transactional, Collection<T>
{
}