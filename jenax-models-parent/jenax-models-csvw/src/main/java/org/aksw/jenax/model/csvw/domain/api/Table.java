package org.aksw.jenax.model.csvw.domain.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.commons.model.csvw.term.CsvwTerms;
import org.apache.jena.rdf.model.Resource;

import java.util.Set;

@ResourceView
public interface Table extends Resource {
    @Iri(CsvwTerms.dialect)
    Dialect getDialect();
    Table setDialect(Dialect dialect);

    @Iri(CsvwTerms.xnull)
    Set<String> getNull();
    Table setNull(Set<String> xnull);

    @Iri(CsvwTerms.url)
    String getUrl();
    Table setUrl(String url);
}
