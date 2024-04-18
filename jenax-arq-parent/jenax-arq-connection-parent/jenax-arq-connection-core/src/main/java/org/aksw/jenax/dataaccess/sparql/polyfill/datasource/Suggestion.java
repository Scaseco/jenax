package org.aksw.jenax.dataaccess.sparql.polyfill.datasource;

import java.io.Serializable;

/**
 * A basic class with basic attributes that represents a suggestion to a user.
 *
 *
 * @param <T> The payload type.
 */
public class Suggestion<T>
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    protected String name;
    protected String comment;

    /** A suggestion can be disabled by default so that a user can click on it. */
    protected boolean isEnabled;
    protected T value;

    protected Suggestion(String name, String comment, boolean isEnabled, T value) {
        super();
        this.name = name;
        this.comment = comment;
        this.isEnabled = isEnabled;
        this.value = value;
    }

    public static <T> Suggestion<T> of(String name, String comment, T value) {
        return of(name, comment, true, value);
    }

    public static <T> Suggestion<T> of(String name, String comment, boolean isEnabled, T value) {
        return new Suggestion<>(name, comment, isEnabled, value);
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public T getValue() {
        return value;
    }
}
