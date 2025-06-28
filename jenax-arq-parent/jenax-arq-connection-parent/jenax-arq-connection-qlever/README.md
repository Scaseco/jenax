

## Summary of Operators

### File Level

```bash
public interface FileOpVisitor<T> {
    T visit(FileOpName op);
    T visit(FileOpTranscode op);
    T visit(FileOpVar op);
    T visit(FileOpOverStreamOp op);
}
```

### Stream Level

```bash
public interface StreamOpVisitor<T>
    extends HasSelf<StreamOpVisitor<T>>
{
    T visit(StreamOpFile op);
    T visit(StreamOpTranscode op);
    T visit(StreamOpContentConvert op);
    T visit(StreamOpConcat op);
    T visit(StreamOpCommand op);
    T visit(StreamOpVar op);
}
```

### Cmd Level

```bash
public interface CmdOpVisitor<T> {
    T visit(CmdOpExec op);
    T visit(CmdOpPipe op);
    T visit(CmdOpGroup op);
    T visit(CmdOpString op);
    T visit(CmdOpSubst op);
    T visit(CmdOpToArg op);
    T visit(CmdOpFile op);
    T visit(CmdOpRedirect op);
}
```

