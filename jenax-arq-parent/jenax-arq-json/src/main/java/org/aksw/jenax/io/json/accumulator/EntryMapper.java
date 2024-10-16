package org.aksw.jenax.io.json.accumulator;

public interface EntryMapper<KI, KO, VO, VI> {
    KO mapKey(KI key);
    VO mapValue(VI value);
}
