package org.aksw.jenax.io.rowset.csv;

import java.io.IOException;
import java.util.List;

import org.aksw.jenax.io.rowset.core.RowSetStreamWriter;
import org.aksw.jenax.io.rowset.core.RowSetStreamWriterBuilderBase;
import org.aksw.jenax.io.rowset.core.RowSetStreamWriterFactory;
import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Context;

public class RowSetStreamWriterCSV implements RowSetStreamWriter {

    public static class RowSetStreamWriterBuilderCSV
        extends RowSetStreamWriterBuilderBase
    {
        @Override
        protected RowSetStreamWriter buildActual() {
            AWriter awriter = getAWriter();
            return new RowSetStreamWriterCSV(awriter, vars, context);
        }
    }

    public static final RowSetStreamWriterFactory factory = () -> new RowSetStreamWriterBuilderCSV();

//    public static RowSetStreamWriterFactory factory = lang -> {
//        if ( !Objects.equals(lang, ResultSetLang.RS_CSV) )
//            throw new ResultSetException("RowSetWriter for CSV asked for a " + lang);
//        return new RowSetWriterCSV();
//    };

    static final String NL          = "\r\n";
    static final String headerBytes = "_askResult" + NL;
    static final String yesString   = "true";
    static final String noString    = "false";

    protected AWriter out;
    protected Context context;
    protected List<Var> vars;
    // protected NodeToLabel nodeToLabel; // XXX Use a NodeFormatter as in RowSetWriterTSV

    protected RowSetStreamWriterCSV(AWriter out, List<Var> vars, Context context) {
        this.out = out;
        this.vars = vars;
        this.context = context;
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public void writeAskResult(boolean booleanResult) {
        try {
            out.write(headerBytes);
            if ( booleanResult )
                out.write(yesString);
            else
                out.write(noString);
            out.write(NL);
        } finally {
            out.flush();
        }
    }

    @Override
    public void writeHeader() throws IOException {
        String sep = null;
        // Convert to Vars and output the header line.
        for ( Var var : vars ) {
            String v = var.getVarName();
            if ( sep != null )
                out.write(sep);
            else
                sep = ",";
            out.write(csvSafe(v));
        }
        out.write(NL);
    }

    @Override public void beginBindings() { }
    @Override public void writeBindingSeparator() { }
    @Override public void endBindings() { }
    @Override public void writeFooter() { }

    @Override
    public void writeBinding(Binding binding) {
        String sep;
        sep = null;

        for ( Var v : vars ) {
            if ( sep != null )
                out.write(sep);
            sep = ",";

            Node n = binding.get(v);
            if ( n != null )
                output(out, n);
        }
        out.write(NL);
    }

    private static void output(AWriter w, Node n) { //  NodeToLabelMap bnodes) {
        // String str = FmtUtils.stringForNode(n) ;
        String str = "?";
        if (n.isLiteral())
            str = n.getLiteralLexicalForm();
        else if (n.isURI())
            str = n.getURI();
        else if (n.isBlank())
            str = "_:" + n.getBlankNodeId().getLabelString();
            // str = bnodes.asString(n);

        str = csvSafe(str);
        w.write(str);
        w.flush();
    }

    static protected String csvSafe(String str) {
        // Apparently, there are CSV parsers that only accept "" as an escaped quote
        // if inside a "..."
        if (str.contains("\"") || str.contains(",") || str.contains("\r") || str.contains("\n"))
            str = "\"" + str.replaceAll("\"", "\"\"") + "\"";
        else if (str.isEmpty())
            // Return the quoted empty string.
            str = "\"\"";
        return str;
    }
}
