package org.aksw.facete.v3.api.path;

import org.aksw.jenax.sparql.relation.api.BinaryRelation;

public interface StepSpecFromRelement2
    extends StepSpec
{
    BinaryRelation getRelement();
}
