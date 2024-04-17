package org.aksw.jena_sparql_api.collection.observable;

import org.apache.jena.sparql.core.DatasetGraphWrapper;


/**
 * Copy from jena4. Not clear whether we can do without this class.
 *
 * A {@code QuadAction} is record of a type of change to a {@code DatasetGraph}.
 * <p>
 * {@code DatasetGraph} are sets of quads.
 * An {@code add} only affects the state of the {@code DatasetGraph}
 * if a quad of the same value was not present,
 * and a {@code delete} only affects the state of the {@code DatasetGraph}
 * if a quad was present.
 * <p>
 * A {@code QuadAction} can be an {@code ADD} or {@code DELETE}, indicating a change
 * to the {@code DatasetGraph} actually occured (this assumes checking is done -
 * {@link DatasetChanges} generators may not check - see implementation for details).
 * Otherwise a {@code NO_ADD}, {@code NO_DELETE} {@code QuadAction} is used.
 *
 * @deprecated Do not use. To see changes to a dataset, use {@link DatasetGraphWrapper} to capture change events.
 */
@Deprecated
public enum QuadAction {
    ADD("A"), DELETE("D"), NO_ADD("#A"), NO_DELETE("#D") ;
    public final String label ;
    QuadAction(String label) { this.label = label ; }
}
