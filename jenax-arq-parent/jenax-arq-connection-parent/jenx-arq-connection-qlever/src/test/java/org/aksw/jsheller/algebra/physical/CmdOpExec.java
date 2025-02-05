package org.aksw.jsheller.algebra.physical;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CmdOpExec
    extends CmdOpN
{
    protected String name;

    public static CmdOpExec of(String... cmd) {
        return of(Arrays.asList(cmd));
        // return ofStrings(cmd[0], Arrays.copyOfRange(cmd, 1, cmd.length));
    }

    public static CmdOpExec of(List<String> cmd) {
        return ofStrings(cmd.get(0), cmd.subList(1, cmd.size()));
    }

    public static CmdOpExec ofStrings(String name, String... args) {
        return ofStrings(name, Arrays.asList(args));
    }

    public static CmdOpExec ofStrings(String name, List<String> args) {
        return new CmdOpExec(name, args.stream().<CmdOp>map(CmdOpString::new).toList());
    }

    public CmdOpExec(String name, List<CmdOp> args) {
        super(args);
        this.name = Objects.requireNonNull(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(CmdOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
