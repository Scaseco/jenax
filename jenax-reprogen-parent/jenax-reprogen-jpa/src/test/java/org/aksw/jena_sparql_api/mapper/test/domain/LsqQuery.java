package org.aksw.jena_sparql_api.mapper.test.domain;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.RdfType;

@RdfType("http://spinrdf.org/sp#Query")
public class LsqQuery {
    @Iri
    protected String iri;

    @Iri("http://lsq.aksw.org/vocab#text")
    protected String text;

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "LsqQuery [text=" + text + "]";
    }
}
