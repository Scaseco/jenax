package org.aksw.jena_sparql_api.langtag.validator.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.aksw.jena_sparql_api.langtag.validator.api.LangTagValidationException;
import org.aksw.jena_sparql_api.langtag.validator.api.LangTagValidator;
import org.apache.jena.langtag.LangTag;
import org.apache.jena.langtag.LangTagRE;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;

public class LangTagValidatorImpl
    implements LangTagValidator
{
    public static final Property IANA_TYPE = ResourceFactory.createProperty("urn:x-key:Type");
    public static final Property IANA_SUBTAG = ResourceFactory.createProperty("urn:x-key:Subtag");

    /*package*/ static final int  idxLanguage  = 0;
    /*package*/ static final int  idxScript    = 1;
    /*package*/ static final int  idxRegion    = 2;
    /*package*/ static final int  idxVariant   = 3;
    /*package*/ static final int  idxExtension = 4;
    /*package*/ static final int  idxPrivateUse = 5;

    private static List<Function<LangTag, String>> langTagFieldAccessors = List.of(
        LangTag::getLanguage,
        LangTag::getScript,
        LangTag::getRegion,
        LangTag::getVariant,
        LangTag::getExtension,
        LangTag::getPrivateUse
    );

    /** Index of valid known values per component of a language tag */
    protected Multimap<Integer, String> index;

    public LangTagValidatorImpl(Multimap<Integer, String> index) {
        super();
        this.index = index;
    }

    public static Multimap<Integer, String> indexLangTagKnownValues(Model langRegistryModel) {
        Multimap<Integer, String> result = HashMultimap.create();

        Map<String, Integer> idxToType = new HashMap<>();
        idxToType.put("language", idxLanguage);
        idxToType.put("script", idxScript);
        idxToType.put("region", idxRegion);
        idxToType.put("variant", idxVariant);
        idxToType.put("extlang", idxExtension);

        Set<Resource> set = langRegistryModel.listSubjectsWithProperty(IANA_TYPE)
                .mapWith(RDFNode::asResource).toSet();

        for (Resource tag : set) {
            String xtype = Optional.ofNullable(tag.getProperty(IANA_TYPE)).map(Statement::getString).orElse(null);
            String xsubtag = Optional.ofNullable(tag.getProperty(IANA_SUBTAG)).map(Statement::getString).orElse(null);
            Integer idx = idxToType.get(xtype);

            if (idx == null) {
                // System.err.println("Unknown type: " + xtype + " on " + tag);
                // Objects.requireNonNull(idx, "Failed to map " + tag);
                continue;
            }

            result.put(idx, xsubtag);
        }

        return result;
    }

    @Override
    public boolean check(String langTag) {
        boolean result = check(langTag, index);
        return result;
    }

    @Override
    public void validate(String langTag) throws LangTagValidationException {
        validate(langTag, index, true);
    }

    public static LangTagValidatorImpl createDefault() {
        Model langTagModel = RDFDataMgr.loadModel("iana-language-subtag-registry.2023-02-26.raw.ttl");
        return create(langTagModel);
    }

    public static LangTagValidatorImpl create(Model langTagModel) {
        Multimap<Integer, String> index = indexLangTagKnownValues(langTagModel);
        return new LangTagValidatorImpl(index);
    }

    public static boolean check(
            String langTag,
            Multimap<Integer, String> index) {
        boolean result;
        try {
            result = validate(langTag, index, false);
        } catch (LangTagValidationException e) {
            // Never happens with flag set to false
            result = false;
        }

        return result;
    }


    public static boolean validate(
            String langTag,
            Multimap<Integer, String> index,
            boolean raiseException) throws LangTagValidationException {
        // String[] parts = LangTagRE.parse(langTag);
        LangTag parts = LangTagRE.create(langTag);
        // System.out.println(Arrays.toString(parts));

        // int[] knownIdxs = new int[] {idxLanguage, idxScript, idxRegion, idxVariant, idxExtension };

        // Valid unless proven otherwise
        boolean result = true;

        if (parts == null) {
            result = false;
            if (raiseException) {
                throw new LangTagValidationException("Failed to parse: " + langTag);
            }
        } else {
            for (int i = 0; i < langTagFieldAccessors.size(); ++i) {
                Function<LangTag, String> accessor = langTagFieldAccessors.get(i);
                // int partId = knownIdxs[i];
                String givenValue = accessor.apply(parts); //  parts[partId];

                if (givenValue == null) {
                    continue;
                }

                Collection<String> knownValidValues = index.get(i);

                boolean isValidValue = knownValidValues.contains(givenValue);

                if (!isValidValue) {
                    result = false;

                    // We could add a 'Did you mean ...?' mechanisms here; would require commons-text
                    // for levenshtein distance

                    if (raiseException) {
                        throw new LangTagValidationException("Value '" + givenValue + "' is not known to be valid for part #" + i); // + " valid values: " + new TreeSet<>(knownValidValues));
                    }

                    break;
                }
            }
        }

        return result;
    }
}
