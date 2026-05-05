package org.aksw.jenax.shellgebra;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import org.aksw.jenax.arq.util.io.RDFConverterMetaDataJena;
import org.aksw.jenax.shellgebra.cmd.ArgParserProviderRapper;
import org.aksw.jenax.shellgebra.cmd.ArgsBuilderRapper;
import org.aksw.jenax.shellgebra.cmd.RapperArgs;
import org.aksw.jenax.shellgebra.cmd.UnsupportedConversionException;
import org.aksw.shellgebra.shim.core.ArgsModular;

public class TestArgValidationRapper {
    @Test
    public void test_success() throws Exception {
        ArgsModular<RapperArgs> args = RapperArgs.parse("-i", "rdfxml", "-o", "trig");
        ArgParserProviderRapper.validate(args.model(), RDFConverterMetaDataJena.get());
    }

    @Test
    public void test_unsupportedConversion() throws Exception {
        ArgsModular<RapperArgs> args = RapperArgs.parse("-i", "jsonld", "-o", "trig");
        assertThrows(UnsupportedConversionException.class, () -> {
            ArgParserProviderRapper.validate(args.model(), ArgsBuilderRapper.getDefaultMetaData());
        });
    }

    @Test
    public void test_unknownInputFormat() throws Exception {
        ArgsModular<RapperArgs> args = RapperArgs.parse("-i", "foobar", "-o", "trig");
        assertThrows(NoSuchElementException.class, () -> {
            ArgParserProviderRapper.validate(args.model(), ArgsBuilderRapper.getDefaultMetaData());
        });
    }

}
