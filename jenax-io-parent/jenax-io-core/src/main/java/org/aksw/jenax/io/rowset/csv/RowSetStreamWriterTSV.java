package org.aksw.jenax.io.rowset.csv;

import java.io.IOException;
import java.util.List;

import org.aksw.jenax.io.rowset.core.RowSetStreamWriter;
import org.aksw.jenax.io.rowset.core.RowSetStreamWriterBuilderBase;
import org.aksw.jenax.io.rowset.core.RowSetStreamWriterFactory;
import org.aksw.jenax.io.rowset.csv.RowSetStreamWriterCSV.RowSetStreamWriterBuilderCSV;
import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterTTL;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Context;

public class RowSetStreamWriterTSV implements RowSetStreamWriter {

    public static class RowSetStreamWriterBuilderTSV
        extends RowSetStreamWriterBuilderBase
    {
        @Override
        protected RowSetStreamWriter buildActual() {
            AWriter awriter = getAWriter();
            NodeFormatter finalNodeFormatter = nodeFormatter != null
                    ? nodeFormatter
                    : new NodeFormatterTTL(null, null);

            return new RowSetStreamWriterTSV(awriter, vars, finalNodeFormatter, context);
        }
    }

    public static final RowSetStreamWriterFactory factory = () -> new RowSetStreamWriterBuilderTSV();


//    public static RowSetStreamWriterFactory factory = lang -> {
//        if (!Objects.equals(lang, ResultSetLang.RS_TSV ) )
//            throw new ResultSetException("RowSetWriter for TSV asked for a "+lang);
//        return new RowSetWriterTSV();
//    };

    private static final String NL           = "\n" ;
    private static final String SEP          = "\t" ;

    private static final String headerBytes  = "?_askResult" + NL;
    private static final String yesString    = "true";
    private static final String noString     = "false";

    protected AWriter out;
    protected Context context;
    protected List<Var> vars;
    protected NodeFormatter nodeFormatter;
    // protected NodeToLabel nodeToLabel;

    protected RowSetStreamWriterTSV(AWriter out, List<Var> vars, NodeFormatter nodeFormatter, Context context) {
        this.out = out;
        this.vars = vars;
        this.context = context;
        this.nodeFormatter = nodeFormatter;
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
        } finally { out.flush(); }
    }

    @Override
    public void writeHeader() {
        String sep = null;
        // writes the variables on the first line
        for ( Var var : vars ) {
            if ( sep != null )
                out.write(sep);
            else
                sep = SEP;
            out.write("?");
            out.write(var.getVarName());
        }
        out.write(NL);
    }

    @Override public void beginBindings() { }
    @Override public void writeBindingSeparator() { }
    @Override public void endBindings() { }
    @Override public void writeFooter() { }

    @Override
    public void writeBinding(Binding binding) {
        String sep = null;

        for ( Var v : vars ) {
            if ( sep != null )
                out.write(sep);
            sep = SEP;

            Node n = binding.get(v);
            if ( n != null ) {
                // This will not include a raw tab.
                nodeFormatter.format(out, n);
            }
        }
        out.write(NL);
    }
}
