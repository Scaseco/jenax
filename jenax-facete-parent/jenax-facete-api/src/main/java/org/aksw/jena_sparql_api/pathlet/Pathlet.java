package org.aksw.jena_sparql_api.pathlet;

import org.aksw.jena_sparql_api.relationlet.Relationlet;
import org.apache.jena.sparql.core.Var;

/**
 * A pathlet is a specialization of a {@link Relationlet}
 * with designated source and target variables
 *
 * @author raven
 *
 */
public interface Pathlet
    extends Relationlet
{
    Var getSrcVar();
    Var getTgtVar();

}