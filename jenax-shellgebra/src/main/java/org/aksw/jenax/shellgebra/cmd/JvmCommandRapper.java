package org.aksw.jenax.shellgebra.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.aksw.commons.io.util.stream.InputStreamTransform;
import org.aksw.jenax.arq.util.io.StreamingRDFConverter;
import org.aksw.shellgebra.shim.cmd.JvmCommandBase;
import org.aksw.shellgebra.shim.core.ArgsModular;
import org.aksw.vshell.registry.JvmExecCxt;
import org.apache.commons.io.input.CloseShieldInputStream;

public class JvmCommandRapper
    extends JvmCommandBase<RapperArgs>
{
    @Override
    public ArgsModular<RapperArgs> parseArgs(String... args) {
        ArgsModular<RapperArgs> result = RapperArgs.parse(args);
        return result;
    }

    @Override
    public void runActual(JvmExecCxt cxt, RapperArgs model) throws IOException {
        InputStreamTransform transform = StreamingRDFConverter.converter(
                model.getInputFormat(), model.getOutputFormat(), model.getBaseUrl());
        try (InputStream in = openInputStream(cxt, model)) {
            transform.apply(in).transferTo(cxt.out().outputStream());
        }
    }

    protected InputStream openInputStream(JvmExecCxt cxt, RapperArgs model) throws IOException {
        String inputFile = model.getInputFile();
        InputStream inputStream;
        if (RapperArgs.readsStdin(model)) {
            inputStream = CloseShieldInputStream.wrap(cxt.in().inputStream());
        } else {
            Path path = Path.of(inputFile);
            inputStream = Files.newInputStream(path);
        }
        return inputStream;
    }
}
