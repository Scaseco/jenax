package org.aksw.commons.util.docker;

import java.util.List;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.vocabulary.OWL;
import org.junit.Test;

// This is more a playground to check if sameAs inference on a tool catalog works
// with jena's mechanisms.
public class TestSameAsByRules {

    @Test
    public void test() {
        Model data = RDFParser.fromString("""
            @prefix :    <http://example.org/> .
            @prefix o:   <http://example.org/ontology#> .
            @prefix ex:  <http://example.org/data#> .
            @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
            @prefix owl: <http://www.w3.org/2002/07/owl#> .

            :Person a owl:Class .
            :Account a owl:Class .
            o:hasId a owl:DatatypeProperty .
            o:country a owl:DatatypeProperty .
            o:localId a owl:DatatypeProperty .

            ex:a rdf:type :Person ; o:hasId "123" .
            ex:b rdf:type :Person ; o:hasId "123" .
            ex:c rdf:type :Person ; o:hasId "999" .

            ex:acc1 rdf:type :Account ; o:country "DE" ; o:localId "A-42" .
            ex:acc2 rdf:type :Account ; o:country "DE" ; o:localId "A-42" .
          """, Lang.TURTLE).build().toModel();

        List<Rule> rules = Rule.parseRules("""
            [Key_Person_hasId:
              (?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://example.org/Person>)
              (?y <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://example.org/Person>)
              (?x <http://example.org/ontology#hasId> ?k)
              (?y <http://example.org/ontology#hasId> ?k)
              notEqual(?x, ?y)
              -> (?x <http://www.w3.org/2002/07/owl#sameAs> ?y)
            ]
        """);

        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
        reasoner.setDerivationLogging(false);
        reasoner.setMode(GenericRuleReasoner.BACKWARD);

        InfModel inf = ModelFactory.createInfModel(reasoner, data);

        // Inspect inferred owl:sameAs statements
        inf.listStatements(null, OWL.sameAs, (RDFNode) null).forEachRemaining(System.out::println);

        // (Optional) persist materialized triples:
        Model m = ModelFactory.createDefaultModel().add(inf);
        m.write(System.out, "TURTLE");
    }
}
