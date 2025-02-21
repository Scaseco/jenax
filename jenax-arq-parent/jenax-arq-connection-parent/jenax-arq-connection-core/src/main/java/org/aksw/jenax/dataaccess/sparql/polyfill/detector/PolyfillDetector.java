package org.aksw.jenax.dataaccess.sparql.polyfill.detector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.polyfill.datasource.Suggestion;
import org.aksw.jenax.model.polyfill.domain.api.PolyfillCondition;
import org.aksw.jenax.model.polyfill.domain.api.PolyfillRewriteJava;
import org.aksw.jenax.model.polyfill.domain.api.PolyfillSuggestionRule;
import org.aksw.jenax.model.polyfill.domain.api.PolyfillVocab;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

public class PolyfillDetector {
    private static class Record {
        protected PolyfillSuggestionRule suggester;
        protected Condition condition;
        protected String javaClass;

        public Record(PolyfillSuggestionRule suggester, Condition condition, String javaClass) {
            super();
            this.suggester = suggester;
            this.condition = condition;
            this.javaClass = javaClass;
        }
    }

    protected NavigableMap<Integer, List<Record>> suggesters = new TreeMap<>();


    public List<Suggestion<String>> detect(RDFDataSource dataSource) {
        List<Suggestion<String>> result = new ArrayList<>();
        for (Entry<Integer, List<Record>> entry : suggesters.entrySet()) {
            for (Record e : entry.getValue()) {
                Condition condition = e.condition;
                PolyfillSuggestionRule suggester = e.suggester;
                boolean value = condition == null ? true : condition.test(dataSource);
                if (value) {
                    Suggestion<String> contrib = Suggestion.of(suggester.getLabel(), suggester.getComment(), e.javaClass);
                    result.add(contrib);
                }
            }
        }
        return result;
    }

    public void load(Model model) {
        ConditionProcessor conditionProcessor = ConditionProcessor.get();

        ExtendedIterator<PolyfillSuggestionRule> it = model.listResourcesWithProperty(PolyfillVocab.suggestion)
                .mapWith(x -> x.as(PolyfillSuggestionRule.class));
        try {
            while (it.hasNext()) {
                PolyfillSuggestionRule suggester = it.next();

                PolyfillCondition conditionData = suggester.getCondition();

                Condition condition = conditionData == null ? null : conditionData.accept(conditionProcessor);

                Integer level = suggester.getLevel();
//                suggester.getLabel();
//                suggester.getComment();
//                suggester.isEnabledByDefault();

                Resource rewriterData = suggester.getSuggestion();
                // Resource suggestion = JenaPluginUtils.polymorphicCast(suggestionRaw);
                if (rewriterData == null) {
                    throw new NullPointerException("Suggestion is not set: " + suggester);
                } else if (rewriterData instanceof PolyfillRewriteJava) {
                    PolyfillRewriteJava tmp = (PolyfillRewriteJava)rewriterData;
                    String javaClass = tmp.getJavaClass();
                    Record record = new Record(suggester, condition, javaClass);

                    int effectiveLevel = level == null ? 1000000 : level.intValue();

                    suggesters.computeIfAbsent(effectiveLevel, k -> new ArrayList<>()).add(record);
                } else {
                    throw new RuntimeException("Unsupported suggestion type: " + rewriterData.getClass());
                }
            }
        } finally {
            it.close();
        }
    }
}
