package org.aksw.jenax.io.kryo.jena;

import java.util.Map;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * A node serializer using a custom (i.e. non-standard) format for intermediate serialization.
 * This class was created in order to allow processing of invalid RDF data.
 * Examples include IRIs with white spaces or even worse IRIs with angular brackets which would
 * result in a non-parsable turtle serialization.
 *
 * @author Claus Stadler
 */
public class GenericNodeSerializerCustom
    extends Serializer<Node>
{
    public static final int TYPE_MASK    = 0x70; // 0111
    public static final int TYPE_IRI     = 0x10; // 0001
    public static final int TYPE_BNODE   = 0x20; // 0010
    public static final int TYPE_LITERAL = 0x30; // 0011
    public static final int TYPE_VAR     = 0x40; // 0100
    public static final int TYPE_TRIPLE  = 0x50; // 0101

    public static final int SUBTYPE_MASK      = 0x03; // 0011
    public static final int LITERAL_HAS_LANG  = 0x01; // 0001
    public static final int LITERAL_HAS_DTYPE = 0x02; // 0010

    // Whether the value field is abbreviated (currently only applies to IRIs)
    public static final int ABBREV_VALUE      = 0x08; // 1000

    // Whether the datatype field is abbreviated
    public static final int ABBREV_DTYPE      = 0x04; // 0100


    public static BidiMap<String, String> prefixToIri = new DualHashBidiMap<>();

    static {
        prefixToIri.put("a", RDF.type.getURI());
        prefixToIri.put("d", Quad.defaultGraphIRI.getURI());
        prefixToIri.put("g", Quad.defaultGraphNodeGenerated.getURI());
        prefixToIri.put("x", XSD.NS);
        prefixToIri.put("r", RDF.uri);
        prefixToIri.put("s", RDFS.uri);
        prefixToIri.put("o", OWL.NS);
    }

    /**
     * Takes a guess for the namespace URI string to use in abbreviation.
     * Finds the part of the IRI string before the last '#' or '/'.
     *
     * @param iriString String string
     * @return String or null
     */
    public static String getPossibleKey(String iriString) {
        int n = iriString.length();
        int i;
        outer: for (i = n - 1; i >= 0; --i) {
            char c = iriString.charAt(i);
            switch(c) {
            case '#':
            case '/':
            case ':':
            case '.':
                // We could add ':' here, it is used as a separator in URNs.
                // But it is a multiple use character and always present in the scheme name.
                // This is a fast-track guess so don't try guessing based on ':'.
                break outer;
            default:
                // break;
            }
        }
        String result = i >= 0 ? iriString.substring(0, i + 1) : null;
        return result;
    }

    public static String encode(Map<String, String> iriToPrefix, String iri) {
        String result = iriToPrefix.get(iri);
        if (result == null) {
            String key = getPossibleKey(iri);
            if (key != null) {
                String prefix = iriToPrefix.get(key);
                if (prefix != null) {
                    result = prefix + ":" + iri.substring(key.length());
                }
            }
        }
        return result;
    }

    public static String decode(Map<String, String> prefixToIri, String curie) {
        String result;
        int idx = curie.indexOf(':');
        if (idx < 0) {
            result = prefixToIri.get(curie);
        } else {
            String prefix = curie.substring(0, idx);
            String iri = prefixToIri.get(prefix);
            result = iri + curie.substring(idx + 1);
        }
        return result;
    }


    protected TypeMapper typeMapper;

    public GenericNodeSerializerCustom() {
        this(TypeMapper.getInstance());
    }

    public GenericNodeSerializerCustom(TypeMapper typeMapper) {
        this.typeMapper = typeMapper;
    }

    @Override
    public void write(Kryo kryo, Output output, Node node) {
        if (node.isURI()) {
            String uri = node.getURI();
            String curie = encode(prefixToIri.inverseBidiMap(), uri);
            if (curie != null) {
                output.writeByte(TYPE_IRI | ABBREV_VALUE);
                output.writeString(curie);
            } else {
                output.writeByte(TYPE_IRI);
                output.writeString(uri);
            }
        } else if (node.isLiteral()) {
            String lex = node.getLiteralLexicalForm();
            String lang = node.getLiteralLanguage();
            String dt = node.getLiteralDatatypeURI();

            if (lang != null && !lang.isEmpty()) {
                output.writeByte(TYPE_LITERAL | LITERAL_HAS_LANG);
                output.writeString(lex);
                output.writeString(lang);
            } else if (dt != null && !dt.isEmpty() && !dt.equals(XSD.xstring.getURI())) {
                String dtCurie = encode(prefixToIri.inverseBidiMap(), dt);
                if (dtCurie != null) {
                    output.writeByte(TYPE_LITERAL | LITERAL_HAS_DTYPE | ABBREV_DTYPE);
                    output.writeString(lex);
                    output.writeString(dtCurie);
                } else {
                    output.writeByte(TYPE_LITERAL | LITERAL_HAS_DTYPE);
                    output.writeString(lex);
                    output.writeString(dt);
                }
            } else {
                output.writeByte(TYPE_LITERAL);
                output.writeString(lex);
            }
        } else if (node.isBlank()) {
            output.writeByte(TYPE_BNODE);
            output.writeString(node.getBlankNodeLabel());
        } else if (node.isVariable()) {
            output.writeByte(TYPE_VAR);
            output.writeString(node.getName());
        } else if (node.isNodeTriple()) {
            output.writeByte(TYPE_TRIPLE);
            kryo.writeObject(output, node.getTriple());
        } else {
            throw new RuntimeException("Unknown node type: " + node);
        }
    }

    @Override
    public Node read(Kryo kryo, Input input, Class<Node> cls) {
        Node result;
        String v1, v2;
        Triple t;

        byte type = input.readByte();

        int typeVal = type & TYPE_MASK;
        switch (typeVal) {
            case TYPE_IRI:
                v1 = input.readString();
                if ((type & ABBREV_VALUE) != 0) {
                    v1 = decode(prefixToIri, v1);
                }
                result = NodeFactory.createURI(v1);
                break;
            case TYPE_LITERAL:
                int subTypeVal = type & SUBTYPE_MASK;
                switch (subTypeVal) {
                    case 0:
                        v1 = input.readString();
                        result = NodeFactory.createLiteral(v1);
                        break;
                    case LITERAL_HAS_LANG:
                        v1 = input.readString();
                        v2 = input.readString();
                        result = NodeFactory.createLiteral(v1, v2);
                        break;
                    case LITERAL_HAS_DTYPE:
                        v1 = input.readString();
                        v2 = input.readString();
                        if ((type & ABBREV_DTYPE) != 0) {
                            v2 = decode(prefixToIri, v2);
                        }
                        RDFDatatype dtype = typeMapper.getSafeTypeByName(v2);
                        result = NodeFactory.createLiteral(v1, dtype);
                        break;
                    default:
                        throw new RuntimeException("Unknown literal sub-type: " + subTypeVal);
                }
                break;
            case TYPE_BNODE:
                v1 = input.readString();
                result = NodeFactory.createBlankNode(v1);
                break;
            case TYPE_VAR:
                v1 = input.readString();
                result = Var.alloc(v1); // NodeFactory.createVariable ?
                break;
            case TYPE_TRIPLE:
                t = kryo.readObject(input, Triple.class);
                result = NodeFactory.createTripleNode(t);
                break;
            default:
                throw new RuntimeException("Unknown node type: " + typeVal);
        }
        return result;
    }
}
