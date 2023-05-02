package org.aksw.jena_sparql_api.conjure.dataref.core.api;

import java.util.List;

public interface DataRefGit
	extends DataRef
{
	String getGitUrl();
	List<String> getFileNamePatterns();
	
	@Override
	default <T> T accept(DataRefVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
