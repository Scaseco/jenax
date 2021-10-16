package org.aksw.jenax.arq.schema_mapping;

import java.util.Map;
import java.util.Set;

interface TypePromoter {
	Map<String, String> promoteTypes(Set<String> datatypeIris);
}