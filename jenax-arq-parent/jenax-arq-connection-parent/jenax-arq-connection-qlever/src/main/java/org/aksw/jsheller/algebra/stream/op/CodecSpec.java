package org.aksw.jsheller.algebra.stream.op;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jsheller.registry.CodecVariant;

public class CodecSpec {
    private String name;
    private List<CodecVariant> variants = new ArrayList<>();

    public CodecSpec(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<CodecVariant> getVariants() {
        return variants;
    }
}
