package org.aksw.jenax.graphql.sparql;

import java.util.List;
import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSources;
import org.aksw.jenax.graphql.schema.generator.GraphQlSchemaGenerator;
import org.aksw.jenax.graphql.schema.generator.GraphQlSchemaGenerator.TypeInfo;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParserBuilder;
import org.junit.Assert;
import org.junit.Test;

import graphql.language.AstPrinter;
import graphql.language.Document;
import graphql.parser.Parser;

public class TestGraphQlSchemaGenerator {

    // TODO My gut feeling is that it might be possible to devise a 'cyclic' test case.
    // Like: there is a resource with two classes,
    // and each class has its own property that is in conflict by having the same types as the resource.


    @Test
    public void test01() {

        // Test case where :TypeA and :TypeB use the same property :p with different target types.

        // This should
        // - create two classes: a_without_p and b_without_p (empty classes must be enabled) and
        // - create a new type x_union_y
        // - a new class A_union_B extends a_without_p, b_without_p { :p x_union_y }
        String dataStr = """
        PREFIX eg: <http://www.example.org/>

        eg:sA
          a eg:TypeA ;
          eg:p eg:oA ;
          .

        eg:sB
          a eg:TypeA ;
          eg:p eg:oB ;
          .

        eg:oA a eg:TypeX .
        eg:oB a eg:TypeY .
        """;

        test(dataStr);
    }

    @Test
    public void testRecursiveSplit() {

        // Test case where :TypeA and :TypeB use the same property :p with different target types.

        // This should
        // - create two classes: a_without_p and b_without_p (empty classes must be enabled) and
        // - create a new type x_union_y
        // - a new class A_union_B extends a_without_p, b_without_p { :p x_union_y }
        String dataStr = """
        PREFIX eg: <http://www.example.org/>

        eg:sA
          a eg:TypeA ;
          eg:p eg:oA ;
          .

        eg:sB
          a eg:TypeA ;
          eg:p eg:oB ;
          .

        eg:oA a eg:TypeX .
        eg:oB a eg:TypeY .
        """;

        test(dataStr);
    }



    private static void test(String dataStr) {
        Graph graph = RDFParserBuilder.create().fromString(dataStr).lang(Lang.TURTLE).toGraph();
        List<TypeInfo> types = GraphQlSchemaGenerator.summarize(RdfDataSources.of(graph));
        System.out.println("Begin of summary:");
        types.forEach(t -> System.out.println(t));
        System.out.println("End of summary.");

        GraphQlSchemaGenerator generator = new GraphQlSchemaGenerator();
        Document document = generator.process(types);
        validateRoundtrip(document);

        String str = AstPrinter.printAst(document);
        System.out.println(str);
    }

    private static Document validateRoundtrip(Document originalDoc) {
        String originalDocStr = AstPrinter.printAst(originalDoc);
        Document copyDoc = Parser.parse(originalDocStr);
        String copyDocStr = AstPrinter.printAst(copyDoc);
        // boolean isEquals = Objects.equals(originalDoc, copyDoc);
        boolean isEquals = Objects.equals(originalDocStr, copyDocStr);
        if (!isEquals) {
            System.err.println("Expected:");
            System.err.println(AstPrinter.printAst(originalDoc));
            System.err.println("Actual:");
            System.err.println(AstPrinter.printAst(copyDoc));
        }
        Assert.assertTrue(isEquals);
        return originalDoc;
    }
}
