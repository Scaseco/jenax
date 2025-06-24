package org.aksw.jenax.graphql.sparql;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiFunction;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.AccContext;
import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AccStateDriver;
import org.aksw.jenax.graphql.sparql.v2.agg.state.impl.AggStateLiteral;
import org.aksw.jenax.graphql.sparql.v2.agg.state.impl.AggStateMap;
import org.aksw.jenax.graphql.sparql.v2.agg.state.impl.AggStateObject;
import org.aksw.jenax.graphql.sparql.v2.agg.state.impl.AggStateProperty;
import org.aksw.jenax.graphql.sparql.v2.api2.Connective;
import org.aksw.jenax.graphql.sparql.v2.api2.ElementGeneratorLateral;
import org.aksw.jenax.graphql.sparql.v2.api2.ElementGeneratorLateral.ElementMapping;
import org.aksw.jenax.graphql.sparql.v2.api2.ElementGeneratorLateral.ElementNodeVarMapping;
import org.aksw.jenax.graphql.sparql.v2.api2.QueryUtils;
import org.aksw.jenax.graphql.sparql.v2.gon.model.GonProviderJava;
import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriterViaGon;
import org.aksw.jenax.graphql.sparql.v2.model.ElementNode;
import org.aksw.jenax.graphql.sparql.v2.rewrite.Bind;
import org.aksw.jenax.graphql.sparql.v2.util.BindingRemapped;
import org.aksw.jenax.graphql.sparql.v2.util.ExecutionContextUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.syntax.Element;

public class TestConnective {
    // @Test
    public void test() {

        // Issues:
        // - Allow "schematic" selections only?
        //     i.e. placeholder nodes that just declare the variables but not the element - and may still have sub-selections.
        //     Fragments always require a spread to link them into the tree. Not sure if fragments are already the solution,
        //     because a spread only points to a fragment, but a spread does not have children.
        //     So its really a field that has a slot for the relation, and one can inject a pair (element, varMap)
        //     that maps the element vars to that of the field.
        //
        // - Allow nodes to carry var mapping information: When we canonicalize the variable naming
        //     Prefix variables with the path to the field / global id
        //     Replace connecting variables with that of the parent
        //	     Alternatively: Inject filter statements (?child_x = ?parent_x)
        //     Var operations are:
        //       RENAME var with another var
        //       BIND var to a constant
        //       BIND var to a another var
        //       The question is whether BINDs refer to before or after renaming.
        //

//        FragmentMemberBuilder fragmentBuilder = FragmentBuilder.newBuilder()
//            // Members of the fragment can connect to all visible vars.
//            .visibleVarNames("src")
//            // By default, adding members will connect to this variable.
//            .targetVarNames("src")
//            .build();
//
//        fragmentBuilder.newFieldBuilder()
//            .newConnectiveBuilder()
//                .fwd(RDFS.label)
//                // .element("{ ?x <urn:myFrag> ?l }")
//                // .connectVarNames("x")
//                // .targetVarNames("l")
//                .set()
//            .parentVarNames("src")
//            .build();
//
//        fragmentBuilder.bwd(RDFS.seeAlso).fwd(RDFS.comment);
//
//        Fragment fragment = fragmentBuilder.build();

        ElementNode root = ElementNode.of(Connective.newBuilder()
                .element("{ ?s a <urn:Person> }")
                .connectVarNames("s")
                .targetVarNames("s")
                .build())
                ;

//        Field knowsFieldA = root.newFieldBuilder()
//            .newConnectiveBuilder()
//                .element("{ ?x <foaf:knows> ?y }")
//                .connectVarNames("x")
//                .targetVarNames("y")
//                .set()
//            .build();

        ElementNode knowsFieldA = root.fwd("foaf:knows");

        ElementNode poField = ElementNode.of(Connective.newBuilder()
                .element("{ ?a ?b ?c }")
                .connectVarNames("a")
                .targetVarNames("b", "c")
                .build());
        knowsFieldA.addChild(poField);

        ElementNode labelField = poField.prepareChild().child(
                ElementNode.of("myLabelField",
                    Connective.newBuilder()
                        .element("{ SELECT * { ?s <urn:label> ?l } LIMIT 10 }")
                        .connectVarNames("s")
                        .targetVarNames("l").build()))
                .parentVarNames("c").attach();

//        poField.newFragmentSpreadBuilder()
//            .setFragment(fragment)
//            .map("src", "c")
//            .build();

        ElementNode spField = ElementNode.of(Connective.newBuilder()
                .element("{ ?c ?b ?a }")
                .connectVarNames("a")
                .targetVarNames("c")
                .build());
        knowsFieldA.addChild(spField);

        //String str = connective.toString();
        System.out.println("Tree");
        System.out.println("------------------------------------------------");
        ElementNode rootField = root;
        System.out.println(rootField);
        System.out.println();

        System.out.println("Element");
        System.out.println("------------------------------------------------");
        // Element elt = ElementGeneratorLateral.toLateral(rootField, PathStr.newRelativePath(), Map.of(), Vars.d);
        // System.out.println(elt);

        // In principle:
        // Each conventional traversal field is populated using its own LATERAL block (field-centric view)
        // This corresponds to traversing the graph along edges, e.g. foaf:knows, rdf:type, etc.
        // 'Hollow' fields however are fields that appear in the output but do not correspond to traversals.
        // Hollow fields are populated within an AccState

        // Hollow fields could be seen as fields that 'traverse' over variables:
        //   Such as: Traverse to the values of ?p.

        // With a field centric view, we can't
        // accumulate { ?s ?p ?o } into { s: { p: [], o[] } } directly.

        // However, we can do


        // Differences between serialization, materalization (stored), and deferred:
        //   A map accumulator in storing mode keeps a Map<K, SubAcc> map.
        //   Exact Defer: The write events are delegated to a writer, and replayed on end.
        //   Defer: The writer has built the GON object internally and replays events from there.
        //
        // How do states and aggregators correspond?
        // Is it:
        // (a) The state needs to delegate to an accumulator.
        // (b) The state IS the accumulator?

        // Remaining issues:
        //   How to tell aggregators that they must be deferring?
        //


        // AggStateProperty.of("id"

        //AggStateObject.of("s2", );

        // AccStateStructure<Binding, FunctionEnv, P_Path0, Node> rootAcc = AccStateLiteral.of(Bind.var("s"));



        // Aggregator that references the AccState:
        // Wheneven an accumulator is created from the aggregator, the AccState is set to that accumulator.


        // AccLateralDriver.of(rootAcc, false, TupleBridgeBinding.of(rootField.getConnective().getConnectVars()));

        // ?s a Person
        //   ?s rdfs:label ?l

        // Create a classic accAccMap.create()
        //   The aggregator


        // When an instance of this aggregator is aggregator is created, it binds to the given acc lateral
        //
        // AggRedirectInput agg = new AggRedirectInput(AccLateralSubInput);

        // Generic: When entering an AccLateral then we know all of the sub-states.

        // Append an AccLateral that delegates

/*
        // Emit an individual ("json") key and the values for it.
        AggPropertyValueBuilder<P_Path0> aggPropValBuilder = AggProperty.newBuilder()
                .element("{ ?s :hasEmployee ?o }")
                .connectVarNames("s")
                .targetVarNames("o")
                // .fieldName(new P_Link(HAS_EMPLOYEE))
                .cardinality(Cardinality.MANY)
                .build();
*/

        // Selection types
        // Field = selection that introduces its own graph pattern.
        // Hollow = selection that refers to an ancestor's graph pattern.
        // Map = selection that produces a map. Output is always
        // Cardinality can always be configured whether to produce an array.

        // Producing nested arrays
        //   @array Annotation to turn fields to array members
        //   @array with @indexBy would add each value for that index as an array member.
        //   myArray @array { // -> [x, y]
        //     x
        //     y
        //   }
        //
        //
        // @sparql(pattern: "SELECT ?x ?y { ... }")
        // row @many {
        //   val @many
        // }
        //
        //

//        AggHollowBuilder builder = null;
//            builder
//                .setName("name");

//        AggObjectBuilder<String> builder = AggObject.<String>newBuilder()
//            .name("myField")
//            .newConnectiveBuilder()
//                .element("{ ?s <hasEmployee> ?o }")
//                .connectVarNames("s")
//                .targetVarNames("o")
//                .set()
//            ;

        // So the idea is that field definitions must be fixed, and adding members to a field can validate against its definition.
        // What's the definition of a hollow field? We want at least be able to ask which variables of the parent are available
        // at its location.

//        ElementMemberBuilder members = builder.build();


//        members.newHollow()
//        	.build();
//
//        AggSelectionBuilder builder = null;
//        builder
//            .newSelection("id") // Start building a new field (error if name already exists)
//            .hollow("name")
//            .field("partners");


        // So are hollow, field and map are different classes or are these just flags on a selection?

        // TODO Add the propValBuilder to an AggObjectBuilder
//        AggObjectBuilder<P_Path0> aggObjBuilder = AggObject.newBuilder()
//           // .parent() // The aggregator to connect to
//           .newMemberBuilder()
//              .fieldName(new P_Link(HAS_EMPLOYEE))
//              .element("{ ?s :hasEmployee ?o }")
//              .connectVarNames("s")
//              .targetVarNames("o")
//              .cardinality(Cardinality.MANY)
//           .build(); //Perhaps set fieldName here
    }

    // @Test Test is by now broken but TestGraphQlTransform is more current
    public void testAgg() {
        // Issues with the tree construction: Who decides on the naming?
        // - If a base name is set, then the parent should allocate the final name.
        // - We need support for aliases. (do we need it on the pattern tree?)
        // - Support for passing ancestor variables to a node.
        //   There are two sides for this:
        //     - child.addVar("ancestorS", ancestor.getVar("s"))
        //     - ancestor.pass("s", child, ancestor) // ancestor.passVar("s").to(child).as("ancestorS")
        //   The first one looks better: we obtain a handle to a var at the parent, and define an alias in the child
        //   How to get a var handle to that field? Do we need a varHandleRef that gets materialized into a varHandle?



        System.out.println("ACTUAL TEST");

        ElementNode root = ElementNode.of(
            "root",
            Connective.newBuilder()
                // The field should probably without distinct and order by - this can be injected by e.g. @index(by: "s")
                .element("{ SELECT DISTINCT ?s { ?s ?p ?o } ORDER BY ?s }")
                .connectVarNames("s")
                .targetVarNames("s")
                .build())
                .setIdentifier("root");


//        root.newFieldBuilder()
//            .newConnectiveBuilder()
//                .element("{ BIND(?x AS ?y) }")
//                .connectVarNames("x")
//                .targetVarNames("y")
//                .set()
//            .build();
//
        ElementNode idField = ElementNode.of(Connective.newBuilder()
                    .element("{ BIND(?x AS ?y) }")
                    .connectVarNames("x")
                    .targetVarNames("y")
                    .build())
                .setIdentifier("field1");


        ElementNode pField = ElementNode.of(Connective.newBuilder()
                    .element("{ ?s ?p ?o }")
                    .connectVarNames("s")
                    .targetVarNames("p")
                    .build())
                .setIdentifier("field2");

        ElementNode oField = ElementNode.of(Connective.newBuilder()
                    .element("{ ?s ?p ?o }")
                    .connectVarNames("s")
                    .targetVarNames("o")
                    .build())
                .setIdentifier("oField")
                .setLabel("oField");

        ElementNode osField = ElementNode.of(Connective.newBuilder()
                    .element("{ ?s ?p ?o }")
                    .connectVarNames("o")
                    .targetVarNames("s")
                    .build())
                .setLabel("osField")
                .setIdentifier("oField_osField");

        // ISSUE Does not work yet; something is broken in the API.
        // Perhaps we need this indirection with selection set:
        // oField.getSelectionSet().add(selection);
        oField.addChild(osField);

        ElementNode rootField = root
            .addChild(idField)
            .addChild(pField)
            .addChild(oField);

        System.out.println(root);

        Var stateVar = Var.alloc("state");
        BiFunction<Binding, FunctionEnv, Object> stateIdExtractor = Bind.var(stateVar).andThen(node -> node); //andThen(node -> node.getLiteralLexicalForm());

        AggStateObject<Binding, FunctionEnv, String, Node> myAgg =
                AggStateObject.of(
                    AggStateMap.of("root", Bind.var("s").andThen(node -> node.toString()), Bind.FALSE().andThen(x -> false)::apply,
                        AggStateObject.of(
        //                    AggStateProperty.one(idField.getName(), "id", AggStateLiteral.of(Bind.var("s"))),
        //                    AggStateProperty.many(pField.getName(), "p", AggStateLiteral.of(Bind.var("p"))),
        //                    AggStateProperty.many(oField.getName(), "o", AggStateLiteral.of(Bind.var("o")))
                                AggStateProperty.one("field1", "id", AggStateLiteral.of(Bind.var("y"))),
                                AggStateProperty.many("field2", "p", AggStateLiteral.of(Bind.var("p"))),
                                AggStateProperty.many("oField", "o", AggStateObject.of(
                                    AggStateProperty.many("oField_osField", "oToS", AggStateLiteral.of(Bind.var("s")))
                                ))
                        )));

        ObjectNotationWriterViaGon<Object, String, Node> writer = ObjectNotationWriterViaGon.of(GonProviderJava.newInstance());
        AccContext<String, Node> cxt = AccContext.serializing(writer);
        AccStateDriver<Binding, FunctionEnv, String, Node> acc = AccStateDriver.of(cxt, myAgg.newAccumulator(), true, stateIdExtractor);
        FunctionEnv env = ExecutionContextUtils.createFunctionEnv();

        Graph graph = RDFParser.fromString("""
            PREFIX : <http://www.example.org/>
            :s1 :p1 :o1 .
            :s1 :p2 :o2 .
            :s2 :p1 :o1 .
            """, Lang.TURTLE)
        .build().toGraph();

        if (false) {
        ElementNodeVarMapping harmonized = ElementGeneratorLateral.harmonizeVariables(rootField, "test_");
        System.out.println("Harmonized:");
        System.err.println(harmonized.stateVarMap());
        System.out.println(harmonized.node());
        }

        ElementMapping eltMap = ElementGeneratorLateral.toLateral(false, rootField, stateVar);
        Element elt = eltMap.element();
        Map<Node, Map<Var, Var>> stateVarMap = eltMap.stateVarMap();

        Query query = QueryUtils.elementToQuery(elt);
        System.out.println(query);

        System.out.println("Result:");
        try (QueryExec qe = QueryExec.newBuilder().graph(graph).query(query).build()) {
            RowSet rs = qe.select();
            while (rs.hasNext()) {
                Binding binding = rs.next();
                Object state = stateIdExtractor.apply(binding, env);
                Map<Var, Var> originalToEnum = stateVarMap.get(state);
                Binding mappedBinding = BindingRemapped.of(binding, originalToEnum);

                // System.out.println(rs.next());
                acc.accumulate(mappedBinding, env);
            }
            acc.end();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Object product = writer.getProduct();
        System.out.println(product);
    }


    public void testHighLevelApi() {
        // Fieldmo
    }
//
//    public static void main(String[] args) {
//        Set<String> test = Set.of("a0", "a1", "b");
//        String str;
//        str = StringUtils.allocateName("a", false, test::contains); // assert "a"
//
//        str = StringUtils.allocateName("a", true, test::contains); // assert "a2"
//
//        str = StringUtils.allocateName("a0", true, test::contains); // assert "a2"
//
//
//        System.out.println(str);
//    }
}
