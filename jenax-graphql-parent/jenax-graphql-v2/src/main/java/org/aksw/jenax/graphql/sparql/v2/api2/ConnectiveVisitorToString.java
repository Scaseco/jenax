package org.aksw.jenax.graphql.sparql.v2.api2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import org.aksw.jenax.graphql.sparql.v2.model.ElementNode;

public class ConnectiveVisitorToString
    implements ConnectiveVisitor<Void>
{
    protected PrintStream out;
    protected String indent;

    public ConnectiveVisitorToString(PrintStream out, String indent) {
        super();
        this.out = out;
        this.indent = indent;
    }

    public static String toString(ConnectiveNode node) {
        String result;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (PrintStream ps = new PrintStream(baos, false, StandardCharsets.UTF_8)) {
                ConnectiveVisitorToString visitor = new ConnectiveVisitorToString(ps, "");
                node.accept(visitor);
                ps.flush();
            }
            baos.flush();
            result = baos.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Should never happen
            throw new RuntimeException(e);
        }
        return result;
    }

    public static void print(PrintStream out, String baseIndent, String baseText, String value) {
        if (value == null) {
            out.print(baseIndent);
            out.println(Objects.toString(value));
        } else {
            String blank = " ".repeat(baseText.length());
            String[] strs = value.split(System.lineSeparator());
            for (int i = 0; i < strs.length; ++i) {
                out.print(baseIndent);
                out.print(i == 0 ? baseText : blank);
                out.println(strs[i]);
            }
        }
    }

    @Override
    public Void visit(Connective connective) {
        print(out, indent + "|  ", "element ", Objects.toString(connective.getElement()));
        // out.println(indent + "|  element " + connective.getElement());
        out.println(indent + "|  default targets " + connective.getDefaultTargetVars());

//        Iterator<Selection> it = connective.getSelections().iterator();
//        int i = 0;
//        while (it.hasNext()) {
//            Selection field = it.next();
//            out.println(indent + "|- field[" + i + "]");
//            String indentBackup = indent;
//            String nextIndent = it.hasNext() ? "|  " : "   ";
//            indent += nextIndent;
//            // entry.getValue().print(out, indent + nextIndent);
//            field.accept(this);
//            indent = indentBackup;
//            ++i;
//        }
        return null;
    }

    @Override
    public Void visit(ElementNode field) {
        Connective connective = field.getConnective();
        String parentStr = field.getJoinLink() == null // field.getParent() == null
                ? "root"
                : "parent." + field.getJoinLink();
        out.println(indent + "|- connects " + parentStr); //+ " to this." + connective.getConnectVars());


        // out.println(indent + "|- element " + connective.getElement());

        visit(connective);

        visitSelections(field.getSelections());
        // Iterator<Selection> it = field.getSelections().iterator();
//        int i = 0;
//        while (it.hasNext()) {
//            Selection subField = it.next();
//            out.println(indent + "|- field[" + i + "]");
//            String indentBackup = indent;
//            String nextIndent = it.hasNext() ? "|  " : "   ";
//            indent += nextIndent;
//            // entry.getValue().print(out, indent + nextIndent);
//            subField.accept(this);
//            indent = indentBackup;
//            ++i;
//        }
        return null;
    }

    protected void visitSelections(Collection<Selection> selections) {
        Iterator<Selection> it = selections.iterator();
        int i = 0;
        String baseIndent = indent;
        while (it.hasNext()) {
            Selection subField = it.next();
            out.println(indent + "|- field[" + i + "] " + subField.getName());
            String indentBackup = indent;
            String nextIndent = it.hasNext() ? "|  " : "   ";
            String tmp = indent + nextIndent;
            indent = tmp;
            // entry.getValue().print(out, indent + nextIndent);
            subField.accept(this);
            indent = indentBackup;
            ++i;
        }
        if (selections.isEmpty()) {
            out.println(baseIndent);
        }
    }

    @Override
    public Void visit(FragmentSpread fragmentSpread) {
        out.println(indent + "|- spreads " + fragmentSpread.getName() + " " + fragmentSpread.getFragmentToInput());
        visitSelections(fragmentSpread.getFragment().getSelections());
        return null;
    }

//    @Override
//    public Void visit(Fragment connective) {
//        // TODO Auto-generated method stub
//        return null;
//    }
}
