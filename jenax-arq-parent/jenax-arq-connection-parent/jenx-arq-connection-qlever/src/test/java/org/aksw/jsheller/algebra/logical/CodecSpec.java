package org.aksw.jsheller.algebra.logical;

import java.util.ArrayList;
import java.util.List;

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
