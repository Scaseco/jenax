package org.aksw.facete.v3.api;

import java.util.Optional;

import org.aksw.commons.util.obj.ObjectUtils;

/**
 * A convenience trait to perform inline casts.
 *
 * @author Claus Stadler, Dec 28, 2018
 *
 */
public interface Castable {
    default <T> T as(Class<T> clazz) {
        T result = ObjectUtils.castAs(clazz, this);
        return result;
    }

    default <T> Optional<T> tryAs(Class<T> clazz) {
        Optional<T> result = ObjectUtils.tryCastAs(clazz, this);
        return result;
    }

    default boolean canAs(Class<?> clazz) {
        boolean result = ObjectUtils.canCastAs(clazz, this);
        return result;
    }

    /*
     * public static <T> T castAs(Class<T> clazz, Object o) {
     *
     * @SuppressWarnings("unchecked") T result = (T)o; return result; }
     */
//	public static boolean canCastAs(Class<?> clazz, Object o) {
//		boolean result = o == null ? true : clazz.isAssignableFrom(o.getClass());
//		return result;
//	}
//
//	public static <T> Optional<T> tryCastAs(Class<T> clazz, Object o) {
//		boolean canCastAs = canCastAs(clazz, o);
//		Optional<T> result = canCastAs ? Optional.of(castAs(clazz,o)) : Optional.empty();
//		return result;
//	}
}
