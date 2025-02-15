package jenax.engine.qlever.cmd.model;

/**
 * The metadata is used to build to transform an OpCodec into an OpCodecCommand.
 */
public interface CommandMetaData {

    // Boolean allows to leave aspects unspecified (will be effectively interpreted as feature not present)

    Boolean supportsFileArg();
    Boolean supportsMultipleFileArgs(); // Implies supportsFile.
    Boolean supportsStdIn();

    /**
     * Whether to prefer process substition over pipe.
     * I.e. prefer {@code processor <(cat data.txt)}
     * over
     * {@code cat data.txt | processor}.
     */
    Boolean preferProcessSubstitution();

    /** */
    Boolean supportsFileOutput();
}
