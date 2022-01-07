package org.aksw.dcat.jena.domain.api;

import java.util.Objects;

public enum DcatIdType {
    FILE,
    DISTRIBUTION,
    DATASET;

    public static DcatIdType of(String idTypeStr) {
        DcatIdType result = Objects.requireNonNull(DcatIdType.valueOf(idTypeStr.toUpperCase()), "Unknown id type" + idTypeStr);
        return result;
    }

}