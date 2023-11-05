package org.aksw.jenax.path.relgen;

import org.aksw.jenax.sparql.fragment.api.Fragment;

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
    protected Fragment template;

    public RelationGeneratorSimple(Fragment template) {
        super();
        this.template = template;
    }

    public static RelationGeneratorSimple create(Fragment template) {
        return new RelationGeneratorSimple(template);
    }

    @Override
    protected Fragment nextInstance() {
        return template;
    }

}
