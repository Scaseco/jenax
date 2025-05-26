package org.aksw.facete.v3.api.path;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import org.aksw.facete.v3.experimental.Resolvers;
import org.aksw.jena_sparql_api.pathlet.Path;
import org.aksw.jena_sparql_api.pathlet.PathletJoinerImpl;
import org.aksw.jena_sparql_api.pathlet.PathletSimple;
import org.aksw.jena_sparql_api.relationlet.Relationlet;
import org.aksw.jena_sparql_api.relationlet.RelationletElementImpl;
import org.aksw.jena_sparql_api.relationlet.RelationletJoinerImpl;
import org.aksw.jena_sparql_api.relationlet.RelationletSimple;
import org.aksw.jena_sparql_api.relationlet.Relationlets;
import org.aksw.jena_sparql_api.relationlet.VarRefStatic;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Ignore;
import org.junit.Test;

public class RelationletTest {

    @Test
    @Ignore
    public void testJoins() {
        RelationletJoinerImpl<Relationlet> joiner = new RelationletJoinerImpl<>();

        if(false) {
            joiner.add("a", Relationlets.from(ElementUtils.createElementTriple(Vars.s, RDF.type.asNode(), Vars.o)).setPinnedVar(Vars.s, true));
            joiner.add("b", Relationlets.from(ElementUtils.createElementTriple(Vars.s, RDFS.label.asNode(), Vars.o)));
            joiner.add("c", Relationlets.from(ElementUtils.createElementTriple(Vars.s, RDFS.isDefinedBy.asNode(), Vars.y)));

            joiner.addJoin("a", Collections.singletonList(Vars.o), "b", Collections.singletonList(Vars.s));
            joiner.addJoin("b", Collections.singletonList(Vars.o), "c", Collections.singletonList(Vars.s));

            joiner.materialize();
        }

        if(false) {
            // Corner case: two independent joins are subsequently affected by another join
            // A.w B.x C.y D.z
            // A.w = B.x
            // C.y = D.z
            // A.w = C.y

            joiner.add("a", Relationlets.from(ElementUtils.createElementTriple(Vars.w, Vars.p, Vars.o)).setPinnedVar(Vars.p, true));
            joiner.add("b", Relationlets.from(ElementUtils.createElementTriple(Vars.x, Vars.p, Vars.o)).setPinnedVar(Vars.x, true));
            joiner.add("c", Relationlets.from(ElementUtils.createElementTriple(Vars.y, Vars.p, Vars.i)).setPinnedVar(Vars.p, true));
            joiner.add("d", Relationlets.from(ElementUtils.createElementTriple(Vars.z, Vars.p, Vars.o)).setPinnedVar(Vars.o, true));

            joiner.addJoin("a", Collections.singletonList(Vars.w), "b", Collections.singletonList(Vars.x));
            joiner.addJoin("c", Collections.singletonList(Vars.y), "d", Collections.singletonList(Vars.z));
            joiner.addJoin("a", Collections.singletonList(Vars.w), "c", Collections.singletonList(Vars.y));

            //joiner.addJoin("a", Collections.singletonList(Vars.w), null, Collections.singletonList(Vars.y));

            joiner.expose("foo", "a", "w");
            joiner.expose("bar", "b", "x");
            RelationletSimple me = joiner.materialize();
//			System.out.println("finalVar: "  + me.getExposedVarToElementVar().get(Var.alloc("foo")));
//			System.out.println("finalVar: "  + me.getExposedVarToElementVar());
            System.out.println("finalVar: "  + me.getNestedVarMap());

        }


        if(true) {
            // Set up a base construct query for extension
            Query baseQuery = QueryFactory.create(
                    "PREFIX eg: <http://www.example.org/>\n" +
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "CONSTRUCT { ?s <http://ex.org/parent> ?o }\n" +
                    "WHERE { ?s eg:test ?o }");

            Resolver resolver = Resolvers.from(baseQuery, Vars.s);

            Path psimple = Path.newPath().fwd(RDF.type);
            Path commonParentPath = Path.newPath().optional().fwd("http://ex.org/parent");

            Path p1 = commonParentPath.fwd("http://ex.org/child", "p1");
            Path p2 = commonParentPath.fwd("http://ex.org/childX", "p1");


            PathletJoinerImpl pathlet = new PathletJoinerImpl(resolver);
            // Add the base query to the pathlet, with variable ?s joining with the pathlet's root
            // and ?s also being the connector for subsequent joins
            pathlet.add(new PathletSimple(Vars.s, Vars.s, new RelationletElementImpl(baseQuery.getQueryPattern()).pinAllVars()));

            // Now add some paths
            pathlet.resolvePath(psimple);
            pathlet.resolvePath(p1);
            pathlet.resolvePath(p2);
            Supplier<VarRefStatic> ref = pathlet.resolvePath(p2.optional().fwd(RDFS.comment));

            RelationletSimple rn = pathlet.materialize();
            System.out.println("Materialized Element: " + rn.getElement());
            System.out.println("Materialized Vars    : " + rn.getExposedVars());

            System.out.println("Plain VarRef: " + ref.get());
            System.out.println("Resolved VarRef: " + rn.resolve(ref.get()));


            //p1.resolveIn(pathlet);

//			pathBuilder.optional()

        }

    }


//	@Test
//	public void fixedLengthPath() {
//		// Set up a base construct query for extension
//		Resolver resolver = Resolvers.create();
//
//		Path psimple = Path.newPath().as("x").fwdVia("p").as("y")
//				.constrain("p", "p = <foobar>");
////				.tp("x", "y", "z");
//
////		Path commonParentPath = Path.newPath().optional().fwd("http://ex.org/parent");
//
//
//	}

    @Test
    public void test() {
        RelationletJoinerImpl<Relationlet> child = new RelationletJoinerImpl<>();
        child.add("a", Relationlets.from(
                ElementUtils.createElementTriple(Vars.s, RDF.type.asNode(), Vars.o)).pinVar(Vars.s));

        child.add("b", Relationlets.from(
                ElementUtils.createElementTriple(Vars.s, RDFS.label.asNode(), Vars.o)));

        child.expose("u", "a", "s");
        child.addJoin("a", Arrays.asList(Vars.s), "b", Arrays.asList(Vars.s));

        RelationletJoinerImpl<Relationlet> parent = new RelationletJoinerImpl<>();
        parent.add("a", child);
        parent.add("c", Relationlets.from(new ElementOptional(ElementUtils.createElementTriple(Vars.x, RDFS.seeAlso.asNode(), Vars.y))));
        parent.addJoin("a", Collections.singletonList(Vars.u), "c", Collections.singletonList(Vars.x));

        RelationletSimple me = parent.materialize();
        System.out.println("Final element:\n" + me.getElement());

//		System.out.println("finalVar: "  + me.getExposedVarToElementVar().get(Var.alloc("foo")));
//		System.out.println("finalVar: "  + me.getExposedVarToElementVar());
        System.out.println("finalVar: "  + me.getNestedVarMap());

        NestedVarMap tmp1 = me.getNestedVarMap().get(Arrays.asList("a", "a"));
        Map<Var, Var> tmp2 = tmp1.getLocalToFinalVarMap();

        // expected: ?s
        System.out.println("Effective var for a.a.s: " + tmp2.get(Vars.s));

        // expected: ?s
        System.out.println("Effective var for a.u " + me.getNestedVarMap().get(Arrays.asList("a")).getLocalToFinalVarMap().get(Vars.u));

    }

}
