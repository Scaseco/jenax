package org.aksw.jenax.path.relgen;

import org.aksw.jenax.sparql.relation.api.Relation;

/**
 * A relation generator that returns the same relation on
 * every request for a new one
 *
 * @author raven
 *
 */
public class RelationGeneratorSimple
    extends RelationGeneratorBase
{
    protected Relation template;

    public RelationGeneratorSimple(Relation template) {
        super();
        this.template = template;
    }

    public static RelationGeneratorSimple create(Relation template) {
        return new RelationGeneratorSimple(template);
    }

    @Override
    protected Relation nextInstance() {
        return template;
    }

}
