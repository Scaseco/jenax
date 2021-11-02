package org.aksw.facete.v3.api.path;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.facete.v3.api.AliasedPathStep;
import org.aksw.jena_sparql_api.concepts.RelationImpl;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;


//interface StepVisitor<T> {
//	T accept(StepUnion step);
//	T accept(StepRelation step);
//}




// What does path resolution mean?
// In essence, its about creating a binary relation - i.e. a sparql element with source and target var.
// The approach for mapping to an element is to first obtain the element from the parent path,
// and then extend it for the current step
//
// The basic traversal steps are simply joins with a triple pattern.
// Giving a step a name allows discrimination of steps that make use of the same triple pattern.
// Every step with different name for the same triplet pattern yields its own instance of that pattern.
// This implies, that the allocated predicate and target variable names
// of steps with different names are disjoint. Setting a manual var name is possible though.
//
//
//
//
//
// [Root].as("a").fwd(prop).
//
// TernaryRelation r = TernaryRelationImpl.parse("?s ?p ?o { regex(?p, 'foobar') }");
//
// fwd() starts a view where the current node acts as the subject of a triplet pattern.
// bwd() starts a view where the current node acts as the object of a triplet pattern.
//
// fwd().via(RDFS.label).one()
//   ?x rdfs:label ?y
//
// fwd().via(Vars.p).has(Vars.p, RDF.type).fixP()
//   ?x ?p ?y FILTER(?p = rdf:type)
//
//
//
// fwd().joinTgt().with(anotherPath, someVar)
//
// traverse via the instance of a relation (the first and last variables are treated as source / target)
// in the case of a ternary relation, the middle variable acts as the predicate
// fwd().via(relation).one()
//
//
//
// Forward to the set of target nodes:
// fwd().via("property").one().as("x")
// fwd().via("property").stepName("a").as("y")
//
// fwd().viaAnyAs("x")
//
// as("x").fwdRel(r).pAs("y").tgtAs("z")
// as("x").fwdRel(r).p().fwd()
//
// as("x").
//
//
//
//

class AliasedPathResolver {

}




class MappedRelation {
    // Remapping of variables
    //BiMap<Var, Var> map;
    Map<Var, Var> innerToOuter;
    Relation r;
}

//interface Step {
//	Relation apply(Relation step);
//}

interface StepRelation
//	extends Step
{
    Relation getRelation();
}

class StepRelationImpl
    implements StepRelation {

    protected Relation r;

//	@Override
    public Relation apply(Relation step) {
        return r;
    }

    @Override
    public Relation getRelation() {
        // TODO Auto-generated method stub
        return null;
    }
}


class OptionalStep {
    public Relation apply(Relation r) {
        return new RelationImpl(new ElementOptional(r.getElement()), r.getVars());
    }
}


//class StepUnion {
//	List<Step> getMembers() { return null; }
//}

class V {
    protected Relation r;
}

class E
    extends DefaultEdge
{
    int type;
    List<Integer> srcJoins;
    List<Integer> tgtJoins;
}

public class StepResolver {




    //Nodelet start;
    V root;
    Graph<V, E> joinGraph;
    //Node startNode;



    public static Element joinGraphToElement(Graph<V, E> joinGraph, V root) {
        Map<V, Relation> vToRelation = new HashMap<>();

        //joinGraph.addV
        Set<E> edges = joinGraph.outgoingEdgesOf(root);
        for(E edge : edges) {
            V src = joinGraph.getEdgeSource(edge);
            V tgt = joinGraph.getEdgeTarget(edge);


            switch(edge.type) {
            case 1: // normal join

                break;
            case 2: // Optional
                ElementOptional elt = new ElementOptional(new ElementGroup());

                break;
            }

        }

        return null;
    }

    //protected Map<>

    public AliasedPathResolver resolve(AliasedPathStep step) {
        boolean isOptional = step.isOptional();
        Relation r = step.getRelation();

        List<Var> vars = r.getVars();
        String alias = step.getAlias();
        Element e = r.getElement();
        //step.isFwd()

        int n = vars.size();

        Var s, p, o = null;
        switch(n) {
        case 2: s = vars.get(0); p = null; o = vars.get(1); break;
        case 3: s = vars.get(0); p = vars.get(1); o = vars.get(2); break;
        default: throw new IllegalArgumentException();
        }

//		Containlet c = start.getTriplet().getContainlet();
//		c.getJoin().of(start.getTriplet()).with()


//		step.getAlias()

        return null;
    }


}
