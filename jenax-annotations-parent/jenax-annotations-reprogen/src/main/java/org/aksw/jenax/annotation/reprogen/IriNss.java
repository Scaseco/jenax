package org.aksw.jenax.annotation.reprogen;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A container for multiple IriNs annotations
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface IriNss {
	IriNs[] value();
}
