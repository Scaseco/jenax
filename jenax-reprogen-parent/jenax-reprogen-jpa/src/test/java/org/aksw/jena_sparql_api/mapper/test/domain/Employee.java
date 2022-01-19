package org.aksw.jena_sparql_api.mapper.test.domain;

import org.aksw.jenax.annotation.reprogen.Inverse;
import org.aksw.jenax.annotation.reprogen.Iri;

public class Employee
	extends PersonOld
{
	@Iri("o:department")
	@Inverse
	private Department department;

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	@Override
	public String toString() {
		return "Employee [department=" + department + "]";
	}
}
