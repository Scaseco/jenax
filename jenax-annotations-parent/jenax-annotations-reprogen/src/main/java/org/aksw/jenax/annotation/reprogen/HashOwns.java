package org.aksw.jenax.annotation.reprogen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation expresses an UML composition for the use of id generation.
 * Composition means that if an entity a owns an entity b then b cannot exist without a.
 * In the context of hash id assignment, it means that an object's <i>final</i> hash id
 * depends on the hash id of its owner.
 *
 * Technically, once a resource's hash id is computed, another depth first pass over all related owned objects is made
 * in order to inject their owner hash id back into the hash ids of the owned objects.
 *
 * A typical use case example is to model maps where map entries with the same key/value are distinguished by the
 * hash id of the owning map.
 *
 * @author raven
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface HashOwns {

}
