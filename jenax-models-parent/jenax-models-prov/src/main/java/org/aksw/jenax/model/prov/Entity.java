package org.aksw.jenax.model.prov;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;

@ResourceView
public interface Entity
    extends ProvComponent
{
    @Iri(ProvTerms.qualifiedDerivation)
    Set<QualifiedDerivation> getQualifiedDerivations();

    default QualifiedDerivation addNewQualifiedDerivation() {
        QualifiedDerivation result = getModel().createResource().as(QualifiedDerivation.class);
        getQualifiedDerivations().add(result);
        return result;
    }
}
