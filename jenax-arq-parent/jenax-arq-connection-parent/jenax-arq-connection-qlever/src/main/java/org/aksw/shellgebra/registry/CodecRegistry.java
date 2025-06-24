package org.aksw.shellgebra.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.aksw.shellgebra.algebra.stream.op.CodecSpec;

// So there are two similar but different views:
// - The 'source' view: Supplier<InputStream> Here we take an op and build an input stream from it.
// - The 'transformer' view: Function<InputStream, InputStream> Here we build a transformation to a byte stream from form A to form B.

public class CodecRegistry {
    private Map<String, CodecSpec> registry = new HashMap<>();


    private static CodecRegistry defaultRegistry = null;

    public static CodecRegistry get() {
        if (defaultRegistry == null) {
            synchronized (CodecRegistry.class) {
                if (defaultRegistry == null) {
                    defaultRegistry = new CodecRegistry();
                    loadDefaults(defaultRegistry);
                }
            }
        }
        return defaultRegistry;
    }

    public CodecSpec getCodecSpec(String name) {
        return registry.get(name);
    }

    public CodecSpec requireCodec(String name) {
        CodecSpec result = getCodecSpec(name);
        if (result == null) {
            throw new NoSuchElementException("No codec with name " + name);
        }
        return result;
    }
    public CodecRegistry add(CodecSpec spec) {
        registry.put(spec.getName(), spec);
        return this;
    }

    public static void loadDefaults(CodecRegistry registry) {
        {
            CodecSpec spec = new CodecSpec("bzip2");
            spec.getVariants().add(CodecVariant.of("lbzip2", "-cd"));
            spec.getVariants().add(CodecVariant.of("bzip2", "-cd"));
            registry.add(spec);
        }

        {
            CodecSpec spec = new CodecSpec("gz");
            spec.getVariants().add(CodecVariant.of("gzip", "-cd"));
            registry.add(spec);
        }

        {
            CodecSpec spec = new CodecSpec("cat");
            spec.getVariants().add(CodecVariant.of("cat"));
            registry.add(spec);
        }
    }

    /*
    public static void main(String[] args) throws IOException {
        CodecRegistry reg = CodecRegistry.get();
        SysRuntime runtime = SysRuntimeImpl.forBash();
        CodecSysEnv env = new CodecSysEnv(runtime);
        CodecTransformSys transform = new CodecTransformSys(reg, env); // , CodecTransformSys.Mode.COMMAND_GROUP);

        CodecOpVisitorStream javaStreamer = CodecOpVisitorStream.getSingleton();

        CodecOp op = new CodecOpFile("/home/raven/tmp/codec-test/test.txt.bz2.gz");
        op = new CodecOpCodecName("gz", op);
        op = new CodecOpCodecName("bzip2", op);
        System.out.println(op);

        try (InputStream xin = op.accept(javaStreamer)) {
            System.out.println(IOUtils.toString(xin, StandardCharsets.UTF_8));
        }

        CodecOp cmd = CodecOpTransformer.transform(op, transform);
        System.out.println(cmd);

        try (InputStream xin = cmd.accept(javaStreamer)) {
            System.out.println(IOUtils.toString(xin, StandardCharsets.UTF_8));
        }

        System.out.println(cmd);
    }
    */
}
