package org.aksw.jena_sparql_api.mapper.test.domain;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.DefaultIri;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.MappedBy;
import org.aksw.jenax.annotation.reprogen.RdfType;

@DefaultIri("o:department-#{name}")
@RdfType("o:Department")
public class Department {
	@Iri("rdfs:label")
	private String name;

	@MappedBy("department")
	private Set<Employee> employees;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Employee> getEmployees() {
		return employees;
	}

	public void setEmployees(Set<Employee> employees) {
		this.employees = employees;
	}

	@Override
	public String toString() {
		return "Department [name=" + name + ", employees=" + employees + "]";
	}
}

