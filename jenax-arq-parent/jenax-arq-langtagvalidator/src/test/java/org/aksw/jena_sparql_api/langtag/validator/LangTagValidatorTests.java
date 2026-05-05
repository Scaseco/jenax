package org.aksw.jena_sparql_api.langtag.validator;

import org.aksw.jena_sparql_api.langtag.validator.impl.LangTagValidators;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LangTagValidatorTests {

	@Test
	public void testKnownValidLangTag() {
		boolean verdict = LangTagValidators.getDefault().check("de-at");
		Assertions.assertTrue(verdict);
	}

	@Test
	public void testInvalidLangTag() {
		boolean verdict = LangTagValidators.getDefault().check("english");
		Assertions.assertFalse(verdict);
	}

}
