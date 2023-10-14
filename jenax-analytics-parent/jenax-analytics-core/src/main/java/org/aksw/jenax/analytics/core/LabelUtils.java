package org.aksw.jenax.analytics.core;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.jenax.arq.aggregation.BestLiteralConfig;
import org.aksw.jenax.arq.aggregation.LiteralPreference;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.aksw.jenax.sparql.fragment.api.Fragment3;
import org.aksw.jenax.sparql.fragment.impl.Fragment3Impl;
import org.aksw.jenax.sparql.fragment.impl.FragmentImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.E_Lang;
import org.apache.jena.sparql.expr.E_LangMatches;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementLateral;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.jena.vocabulary.RDFS;

public class LabelUtils {

    public static Fragment3 createRelationLiteralPreference(LiteralPreference literalPreference) {
        BestLiteralConfig blc = new BestLiteralConfig(literalPreference, Vars.x, Vars.y, Vars.z);
        Fragment3 result = createRelationPrefLabels(blc);
        return result;
    }


    public static Fragment3 createRelationPrefLabels(BestLiteralConfig bestLiteralConfig) {

        List<String> prefLangs = bestLiteralConfig.getLangs();
        List<Node> prefPreds = bestLiteralConfig.getPredicates();

        Var s = bestLiteralConfig.getSubjectVar();
        Var p = bestLiteralConfig.getPredicateVar();
        Var o = bestLiteralConfig.getObjectVar();

        Expr labelExpr = new ExprVar(o);

        // Second, create the element
        List<Expr> langTmp = prefLangs.stream().map(lang -> {
            Expr r = new E_LangMatches(new E_Lang(labelExpr), NodeValue.makeString(lang));
            return r;
        }).collect(Collectors.toList());

        // Combine multiple expressions into a single logicalOr expression.
        Expr langConstraint = ExprUtils.orifyBalanced(langTmp);
        Expr propFilter = ExprUtils.oneOf(p, prefPreds);

        ElementGroup els = new ElementGroup();
        els.addTriplePattern(new Triple(s, p, o));
        els.addElementFilter(new ElementFilter(propFilter));
        els.addElementFilter(new ElementFilter(langConstraint));

        //var result = new Concept(langElement, s);
        Fragment3 result = new Fragment3Impl(els, s, p, o);
        return result;
    }

    /**
     * Create a Fragment2 of the form (?entity, ?bestLabel)
     * Links each entity to its best label according to simple {@link BestLiteralConfig} model:
     * The model simply captures lists of properties and languages in descending priority.
     *
     * Always only returns a single lable:
     * If there are multiple labels under the same property with a suitable language then only one of them will
     * be returned.
     *
     * <pre>
     * ?x ?z | { SELECT
     *   WHERE
     *     { VALUES ( ?y ?propertyScore ) {
     *         ( <http://www.w3.org/2000/01/rdf-schema#label> 0 )
     *         ( <http://www.w3.org/2000/01/rdf-schema#comment> 1 )
     *         ( <http://www.w3.org/2000/01/rdf-schema#seeAlso> 2 )
     *       }
     *       ?x  ?y  ?z
     *       LATERAL
     *         { VALUES ( ?lang ?langScore ) {
     *             ( "en" 0 )
     *             ( "de" 1 )
     *             ( "" 2 )
     *           }
     *           FILTER langMatches(lang(?z), ?lang)
     *         }
     *     }
     *   ORDER BY ASC(?langScore) ASC(?propertyScore)
     *   LIMIT   1
     * }
     *  </pre>
     */
    // We could also expose internal info using a Fragment4 of the form (?entity, ?bestLabel, ?bestLabelLang, ?bestLabelProperty).
    public static Fragment2 createFragmentPrefLabelsLateral(LiteralPreference config) {

        Var propertyVar = Vars.y; // Var.alloc("property");
        Var propertyScoreVar = Var.alloc("propertyScore");
        Var langVar = Var.alloc("lang");
        Var langScoreVar = Var.alloc("langScore");

        // FIXME finish implementation
        Table langTable;
        {
            langTable = TableFactory.create(Arrays.asList(langVar, langScoreVar));
            int i = 0;
            for (String lang : config.getLangs()) {
                langTable.addBinding(BindingFactory.binding(
                    langVar, NodeFactory.createLiteral(lang),
                    langScoreVar, NodeFactoryExtra.intToNode(i)));
                ++i;
            }
        }

        Table propertyTable;
        {
            propertyTable = TableFactory.create(Arrays.asList(propertyVar, propertyScoreVar));
            int i = 0;
            for (Node property : config.getPredicates()) {
                propertyTable.addBinding(BindingFactory.binding(
                    propertyVar, property,
                    propertyScoreVar, NodeFactoryExtra.intToNode(i)));
                ++i;
            }
        }

        Element scoreElt = ElementUtils.groupIfNeeded(
            ElementUtils.create(propertyTable),
            ElementUtils.createElementTriple(Vars.x, propertyVar, Vars.z),
            new ElementLateral(ElementUtils.groupIfNeeded(
                ElementUtils.create(langTable),
                new ElementFilter(new E_LangMatches(new E_Lang(new ExprVar(Vars.z)), new ExprVar(langVar)))
            ))
        );

        Query query = new Query();
        query.setQuerySelectType();
        query.setQueryPattern(scoreElt);
        query.setLimit(1);

        Expr langScoreEv = new ExprVar(langScoreVar);
        Expr propertyScoreEv = new ExprVar(propertyScoreVar);
        if (config.isPreferProperties()) {
            // TODO If property is more important than language then we could generate the pattern
            // in a more efficient way such that properties are matched with a "limit 1"
            query.addOrderBy(propertyScoreEv, Query.ORDER_ASCENDING);
            query.addOrderBy(langScoreEv, Query.ORDER_ASCENDING);
        } else {
            query.addOrderBy(langScoreEv, Query.ORDER_ASCENDING);
            query.addOrderBy(propertyScoreEv, Query.ORDER_ASCENDING);
        }

        Fragment2 result = FragmentImpl.create(new ElementSubQuery(query), Vars.x, Vars.z).toFragment2();
        return result;
    }


    public static void main(String args[]) {
        LiteralPreference pref = new LiteralPreference(
            Arrays.asList("en", "de", ""),
            Arrays.asList(RDFS.label.asNode(), RDFS.comment.asNode(), RDFS.seeAlso.asNode()),
            false);
        Fragment fragment = createFragmentPrefLabelsLateral(pref);
        System.out.println(fragment);
    }
}

