package org.aksw.jena_sparql_api.mapper.test.domain;

import java.util.Calendar;

import org.aksw.jenax.annotation.reprogen.DefaultIri;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.RdfType;

// TODO Remove this class
@RdfType("schema:Person")
@DefaultIri("dbr:#{name}")
public class PersonOld {
    @Iri("rdfs:label")
    private String name;

    @Iri("dbo:birthDate")
    private Calendar birthDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Calendar getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Calendar birthDate) {
        this.birthDate = birthDate;
    }
}
