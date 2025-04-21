package org.aksw.jenax.graphql.sparql.v2.schema;

public interface Generator<T> {
  T next();
  T current();

  /**
   * Clones should independently yield the same sequences of items as the original object
   *
   * @return
   */
  Generator<T> clone(); // throws CloneNotSupportedException;
}
