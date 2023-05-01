package org.aksw.jenax.arq.util.tuple;

import java.util.function.Function;

import org.aksw.commons.tuple.accessor.TupleAccessor;
import org.aksw.commons.tuple.bridge.TupleBridge;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;

/**
 *
 * @author Claus Stadler 11/09/2020
 *
 */
public class TupleOps {


    /**
     * Prepare and return a mapping function that projects specified components into {@link Tuple}s
     * from input tuple-like objects.
     *
     *
     * @param <DomainType> The type of the tuple like object
     * @param <ComponentType> The type of the components of the tuple
     * @param project The indices of the projected components
     * @param accessor The accessor for the tuple tike object
     * @return
     */
    public static <DomainType, ComponentType> Function<DomainType, Tuple<ComponentType>>
    createProjector(int[] project, TupleAccessor<? super DomainType, ? extends ComponentType> accessor) {
        Function<DomainType, Tuple<ComponentType>> result;

        int len = project.length;
        switch(len) {
        case 1: result = domain -> TupleFactory.create1(
                accessor.get(domain, project[0])); break;
        case 2: result = domain -> TupleFactory.create2(
                accessor.get(domain, project[0]),
                accessor.get(domain, project[1])); break;
        case 3: result = domain -> TupleFactory.create3(
                accessor.get(domain, project[0]),
                accessor.get(domain, project[1]),
                accessor.get(domain, project[2])); break;
        case 4: result = domain -> TupleFactory.create4(
                accessor.get(domain, project[0]),
                accessor.get(domain, project[1]),
                accessor.get(domain, project[2]),
                accessor.get(domain, project[3])); break;
        default: result = domain -> project(project, domain, accessor); break;
        }

        return result;
    }

    /**
     * Generic projection method
     *
     * @param <DomainType>
     * @param <ComponentType>
     * @param proj
     * @param domainObject
     * @param accessor
     * @return
     */
    public static <DomainType, ComponentType> Tuple<ComponentType> project(
            int[] proj,
            DomainType domainObject,
            TupleAccessor<? super DomainType, ? extends ComponentType> accessor) {

        // FIXME This cast is broken and will not work!
        @SuppressWarnings("unchecked")
        ComponentType[] tuple = (ComponentType[])new Object[proj.length];
        for(int i = 0; i < proj.length; ++i) {
            tuple[i] = accessor.get(domainObject, proj[i]);
        }
        return TupleFactory.create(tuple);
    }


    /**
     * Return a function that converts domain objects to (jena) tuples.
     *
     * @param <DomainType>
     * @param <ComponentType>
     * @param proj
     * @param domainObject
     * @param accessor
     * @return
     */
    public static <D, C> Function<D, Tuple<C>> tupelizer(
            TupleBridge<? super D, ? extends C> accessor) {
        Function<D, Tuple<C>> result;

        int len = accessor.getDimension();
        switch(len) {
        case 1: result = domain -> TupleFactory.create1(
                accessor.get(domain, 0)); break;
        case 2: result = domain -> TupleFactory.create2(
                accessor.get(domain, 0),
                accessor.get(domain, 1)); break;
        case 3: result = domain -> TupleFactory.create3(
                accessor.get(domain, 0),
                accessor.get(domain, 1),
                accessor.get(domain, 2)); break;
        case 4: result = domain -> TupleFactory.create4(
                accessor.get(domain, 0),
                accessor.get(domain, 1),
                accessor.get(domain, 2),
                accessor.get(domain, 3)); break;
        default: result = domain -> TupleFactory.create(accessor.toComponentArray(domain)); break;
        }

        return result;
    }
}