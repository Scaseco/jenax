package org.aksw.jenax.facete.treequery2.impl;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.aksw.facete.v3.api.VarScope;
import org.aksw.facete.v4.impl.FacetPathUtils;
import org.aksw.jenax.facete.treequery2.api.FacetPathMapping;
import org.aksw.jenax.facete.treequery2.api.ScopedFacetPath;
import org.aksw.jenax.facete.treequery2.api.ScopedVar;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

/**
 * Mapping of facet paths to hashes. In case of hash clashes a sequence number
 * is appended to the hash codes.
 */
public class FacetPathMappingImpl
    implements FacetPathMapping
{
    public static final BaseEncoding DEFAULT_ENCODING = BaseEncoding.base32().omitPadding();

    // murmur3_32_fixed(); // FIXME There seems to be some conflict with hadoop/spark which causes murmur hash not to be found
    public static final HashFunction DEFAULT_HASH_FUNCTION = Hashing.md5();

    private static final Logger logger = LoggerFactory.getLogger(FacetPathMappingImpl.class);

    /** So far allocated mappings */
    protected BiMap<FacetPath, HashCode> pathToHashCode = HashBiMap.create();

    protected HashFunction hashing;
    protected BaseEncoding encoding;

    /**
     * Default construction using Guava's Hashing.murmur3_32_fixed()
     * and BaseEncoding.base32().omitPadding().
     */
    public FacetPathMappingImpl() {
        this(DEFAULT_HASH_FUNCTION, DEFAULT_ENCODING);
    }

    public FacetPathMappingImpl(HashFunction hashing, BaseEncoding encoding) {
        super();
        this.hashing = hashing;
        this.encoding = encoding;
    }

    public BiMap<FacetPath, HashCode> getPathToName() {
        return pathToHashCode;
    }

    public BaseEncoding getEncoding() {
        return encoding;
    }

    public HashFunction getHashing() {
        return hashing;
    }

    // XXX We could always allocate names for all intermediate paths
//    public String allocate(FacetPath path, FacetStep step) {
//
//    }

    public byte[] increment(byte[] arr) {
        byte[] result = Arrays.copyOf(arr, arr.length);
        for (int i = arr.length - 1; i >= 0; --i) {
            byte before = result[i];
            byte after = result[i] += 1;
            if (after > before) {
                break;
            }
        }
        return result;
    }

    @Override
    public String allocate(FacetPath rawFacetPath) {
        FacetPath facetPath = FacetPathUtils.toElementId(rawFacetPath);
        HashCode hc = pathToHashCode.computeIfAbsent(facetPath, fp -> {
            HashCode hashCode = hashing.hashString(facetPath.toString(), StandardCharsets.UTF_8);
            BiMap<HashCode, FacetPath> nameToPath = pathToHashCode.inverse();
            while (true) {
                FacetPath clashPath = nameToPath.get(hashCode);
                if (clashPath == null) {
                    break;
                } else {
                    // log level debug?
                    logger.info("Mitigated hash clash: Hash " + hashCodeToString(hashCode) + " clashed for [" + fp + "] and [" + clashPath + "]");
                }
                hashCode = HashCode.fromBytes(increment(hashCode.asBytes()));;
            }
            return hashCode;
        });
        String result = hashCodeToString(hc);
        return result;
    }

    public static String toString(HashCode hashCode) {
        byte[] bytes = hashCode.asBytes();
        String result = DEFAULT_ENCODING.encode(bytes).toLowerCase();
        return result;
    }

    public String hashCodeToString(HashCode hashCode) {
        byte[] bytes = hashCode.asBytes();
        String result = encoding.encode(bytes).toLowerCase();
        return result;
    }

    public static ScopedVar resolveVar(FacetPathMapping facetPathMapping, ScopedFacetPath sfp) {
        return resolveVar(facetPathMapping, sfp.getScope(), sfp.getFacetPath());
    }

    public static ScopedVar resolveVar(FacetPathMapping facetPathMapping, VarScope scope, FacetPath facetPath) {
        return resolveVar(facetPathMapping, scope.getScopeName(), scope.getStartVar(), facetPath);
    }

    public static ScopedVar resolveVar(FacetPathMapping facetPathMapping, String baseScopeName, Var rootVar, FacetPath facetPath) {
        ScopedVar result;
        // Empty path always resolves to the root var
        if (facetPath.getParent() == null) {
            result = ScopedVar.of("", "", rootVar);
        } else {
            FacetPath parentPath = facetPath.getParent();
            FacetStep lastStep = facetPath.getFileName().toSegment();
            Node component = lastStep.getTargetComponent();
            // FacetPath eltId = FacetPathUtils.toElementId(facetPath);
            if (FacetStep.isSource(component)) {
                result = resolveVar(facetPathMapping, baseScopeName, rootVar, parentPath);
            } else {
                String pathScope = facetPathMapping.allocate(facetPath);
                result = ScopedVar.of(baseScopeName, pathScope, (Var)component);
            }
        }
        return result;
    }
}
