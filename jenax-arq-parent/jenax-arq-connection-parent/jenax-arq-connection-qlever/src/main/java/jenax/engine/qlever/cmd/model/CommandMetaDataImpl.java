package jenax.engine.qlever.cmd.model;

public record CommandMetaDataImpl(
    Boolean supportsStdIn,
    Boolean supportsFileArg,
    Boolean supportsMultipleFileArgs,
    Boolean supportsFileOutput,
    Boolean supportsStreamOutput,
    Boolean preferFileArg,
    Boolean preferFileOutput,
    Boolean preferProcessSubstitution // over pipe
)
implements CommandMetaData { }
