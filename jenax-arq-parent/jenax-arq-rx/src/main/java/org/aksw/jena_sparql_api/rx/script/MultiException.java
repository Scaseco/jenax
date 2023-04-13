package org.aksw.jena_sparql_api.rx.script;

import com.codepoetics.protonpack.StreamUtils;
import org.apache.jena.riot.Lang;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MultiException extends Exception {
    private final Collection<Map.Entry<Lang, Throwable>> langParseExceptions;
    private final Collection<Throwable> stmtParseExceptions;

    public MultiException(String message, Collection<Map.Entry<Lang, Throwable>> langParseExceptions , Collection<Throwable> stmtParseExceptions ) {
        super(message);
        this.langParseExceptions = langParseExceptions;
        langParseExceptions.forEach(e -> addSuppressed(e.getValue()));
        this.stmtParseExceptions = stmtParseExceptions;
        stmtParseExceptions.forEach(this::addSuppressed);
        if (langParseExceptions.size() + stmtParseExceptions.size() == 1) {
            initCause(stmtParseExceptions.isEmpty() ? langParseExceptions.iterator().next().getValue() : stmtParseExceptions.iterator().next());
        }
    }

    public Collection<Map.Entry<Lang, Throwable>> getLangParseExceptions() {
        return langParseExceptions;
    }

    public Collection<Throwable> getStmtParseExceptions() {
        return stmtParseExceptions;
    }

    public static String getEMessage(Throwable e) {
        //return ExceptionUtils.getRootCauseMessage(e);
        String eMessage = e.getMessage();
        Matcher m = eMessage != null ? Pattern.compile("^(\\w+(\\.\\w+)+: )+(.*)$").matcher(eMessage) : null;
        if (m != null && m.find())
            return m.group(1) + m.group(3);
        return e.getClass().getName() + (eMessage == null ? "" : ": " + eMessage);
    }

    @Override
    public String getMessage() {
        String langMessages = "Language parse problems:\n" + langParseExceptions.stream()
                .filter(e -> e.getValue().getMessage() != null)
                .map(
                e -> e.getKey().toString() + ": " + getEMessage(e.getValue()))
                .collect(Collectors.joining("\n"));
        String stmtMessages = "Statement parse problems:\n" + StreamUtils.zipWithIndex(stmtParseExceptions.stream())
                .filter(e -> e.getValue().getMessage() != null)
                .map(
                        e -> e.getIndex() + ": " + getEMessage(e.getValue()))
                .collect(Collectors.joining("\n"));
        return super.getMessage() + "\n" +
                (!langParseExceptions.isEmpty() ? langMessages : "") +
                (!langParseExceptions.isEmpty() && !stmtParseExceptions.isEmpty() ? "\n\n" : "") +
                (!stmtParseExceptions.isEmpty() ? stmtMessages : "");
    }

    @Override
    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
        langParseExceptions.forEach(e -> {
            s.println("Language parse problem (" + e.getKey().getName() + ")");
            e.getValue().printStackTrace(s);
        } );
        stmtParseExceptions.forEach(ex -> {
            s.println("Statement parse problem"); ex.printStackTrace(s);
        });
    }
}
