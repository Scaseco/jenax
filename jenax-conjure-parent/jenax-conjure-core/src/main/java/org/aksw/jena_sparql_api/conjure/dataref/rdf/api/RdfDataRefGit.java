package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import java.util.List;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefGit;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.RdfType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Model;

/**
 * Reference to data stored in a git repository
 * Globbing is allowed
 *
 * @author raven
 *
 */
@ResourceView
@RdfType("rpif:DatRefGit")
public interface RdfDataRefGit
    extends DataRefGit, RdfDataRef
{
    @Iri("rpif:gitUrl")
    @IriType
    RdfDataRefGit setGitUrl(String gitUrl);

    @Override
    @Iri("rpif:fileNamePatterns")
    List<String> getFileNamePatterns();
    RdfDataRefGit setFileNamePatterns(List<String> fileNamePatterns);

    @Override
    default <T> T acceptRdf(RdfDataRefVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    public static RdfDataRefGit create(Model model, String gitUrl, List<String> fileNamePatterns) {
        RdfDataRefGit result = model.createResource().as(RdfDataRefGit.class)
                .setGitUrl(gitUrl)
                .setFileNamePatterns(fileNamePatterns);

        return result;

    }
}
