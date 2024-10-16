package org.aksw.jenax.arq.util.syntax;

import java.math.BigInteger;

import com.google.common.hash.HashCode;

/** A class that bundles a hash code with a lehmer value. */
public class LehmerHash {
    protected HashCode hash;
    protected BigInteger lehmer;

    protected LehmerHash(HashCode hash, BigInteger lehmer) {
        super();
        this.hash = hash;
        this.lehmer = lehmer;
    }

    public static LehmerHash of(HashCode hash, BigInteger lehmer) {
        return new LehmerHash(hash, lehmer);
    }

    public HashCode getHash() {
        return hash;
    }
    public BigInteger getLehmer() {
        return lehmer;
    }
}
