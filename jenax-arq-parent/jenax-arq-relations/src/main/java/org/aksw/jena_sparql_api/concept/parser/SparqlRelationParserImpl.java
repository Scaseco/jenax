package org.aksw.jena_sparql_api.concept.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jenax.stmt.parser.element.SparqlElementParser;
import org.aksw.jenax.stmt.parser.element.SparqlElementParserImpl;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

/**
 * There are multiple ways to represent a relation:
 * - as a path of property uris (TODO we could extend this with a propery path language), "foaf:knows"
 * - as two variables, followed by a '|' and a graph pattern, e.g. "?s ?o | ?s foaf:knows ?o"
 *
 *
 * @author raven
 *
 */
public class SparqlRelationParserImpl
    implements SparqlRelationParser
{
    private Function<String, Element> elementParser;

    public SparqlRelationParserImpl() {
        this(new SparqlElementParserImpl());
    }

    public SparqlRelationParserImpl(Function<String, Element> elementParser) {
        this.elementParser = elementParser;
    }

    @Override
    public BinaryRelation apply(String input) {
        BinaryRelation result = parse(input, elementParser);
        return result;
    }

    public static String VAR_PATTERN_STR = "\\?(\\w|_)+";
    public static Pattern VAR_PATTERN = Pattern.compile(VAR_PATTERN_STR);
    //public static Pattern VAR_PATTERN = Pattern.compile("\\s*(" + VAR_PATTERN_STR + "\\s+" + VAR_PATTERN_STR + ")\\s*");

    public static BinaryRelation parse(String relationStr, Function<String, Element> elementParser) {
        String[] splits = relationStr.split("\\|", 2);
        if(splits.length != 2) {
            throw new RuntimeException("Invalid string: " + relationStr);
        }

        String varsStr = splits[0];
        Matcher m = VAR_PATTERN.matcher(varsStr);
        List<Var> vars = new ArrayList<Var>();
        while(m.find()) {
            String varName = m.group(1);
            Var v = Var.alloc(varName);
            vars.add(v);
        }
        if(vars.size() != 2) {
            throw new RuntimeException("Exactly 2 variables expected, instead got: " + varsStr);
        }

        Var sourceVar = vars.get(0);
        Var targetVar = vars.get(1);


        String elementStr = splits[1];

        Element element = elementParser.apply(elementStr);

        BinaryRelation result = new BinaryRelationImpl(element, sourceVar, targetVar);

        return result;
    }

    public static SparqlRelationParserImpl create(Syntax syntax, Prologue prologue) {
        SparqlElementParser elementParser = SparqlElementParserImpl.create(syntax, prologue);
        SparqlRelationParserImpl result = new SparqlRelationParserImpl(elementParser);
        return result;
    }
}

