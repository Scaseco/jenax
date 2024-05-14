package org.aksw.jena_sparql_api.update;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.aksw.jenax.arq.util.quad.QuadUtils;
import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;

public class QuadContainmentUtils {

    public static Set<Quad> checkContainment(QueryExecutionFactory qef, Iterable<Quad> iterable) {
        Set<Quad> result = checkContainment(qef, iterable.iterator());
        return result;
    }

    public static Set<Quad> checkContainment(QueryExecutionFactory qef, Iterator<Quad> it) {

        Set<Quad> result = new HashSet<Quad>();

        Query query = createQueryCheckExistenceValues(it);
        System.out.println("Containment Check Query: " + query);
        QueryExecution qe = qef.createQueryExecution(query);
        ResultSet rs = qe.execSelect();
        while(rs.hasNext()) {
            Binding binding = rs.nextBinding();

            Node g = binding.get(Vars.g);
            if(g == null) {
                g = Quad.defaultGraphNodeGenerated;
            }

            Node s = binding.get(Vars.s);
            Node p = binding.get(Vars.p);
            Node o = binding.get(Vars.o);

            Quad quad = new Quad(g, s, p, o);
            result.add(quad);
        }

        return result;
    }


    public static ElementData tableToElement(Table table) {
        ElementData result = new ElementData();
        for(Var v : table.getVars()) {
            result.add(v);
        }
//        result.add(Vars.g);
//        result.add(Vars.s);
//        result.add(Vars.p);
//        result.add(Vars.o);

        Iterator<Binding> it = table.rows();
        while(it.hasNext()) {
            Binding binding = it.next();
            result.add(binding);
        }

        return result;
    }

    public static Query createQueryCheckExistenceValues(Iterator<Quad> it) {
        Tables tables = createTablesForQuads(it);

        boolean useG = !tables.getNamedGraphTable().isEmpty();


        Element element = createElement(tables);

        Query result = new Query();
        result.setQuerySelectType();

        if(useG) {
            result.getProject().add(Vars.g);
        }

        result.getProject().add(Vars.s);
        result.getProject().add(Vars.p);
        result.getProject().add(Vars.o);

        result.setQueryPattern(element);

        return result;
    }

    public static Element createElement(Tables tables) {
        Table defaultGraphTable = tables.getDefaultGraphTable();
        Table namedGraphTable = tables.getNamedGraphTable();

        boolean isDefaultGraph = !defaultGraphTable.isEmpty();
        boolean isNamedGraph = !namedGraphTable.isEmpty();
        //boolean useUnion = ! && !defaultGraphTable.isEmpty();

        Element e1 = isDefaultGraph ? createElementDefaultGraph(defaultGraphTable) : null;
        Element e2 = isNamedGraph ? createElementNamedGraph(namedGraphTable) : null;

        Element result;

        if(e1 != null && e2 != null) {
            ElementUnion tmp = new ElementUnion();
            tmp.addElement(e1);
            tmp.addElement(e2);
            result = tmp;
        } else if(e1 != null) {
            result = e1;
        } else if(e2 != null) {
            result = e2;
        } else {
            result = null;
        }

        return result;
    }

    public static Element createElementDefaultGraph(Table table) {
        Element elData = tableToElement(table);

        ElementTriplesBlock elTriples = new ElementTriplesBlock();
        elTriples.addTriple(Triple.create(Vars.s, Vars.p, Vars.o));

        ElementGroup result = new ElementGroup();
        result.addElement(elData);
        result.addElement(elTriples);

        return result;
    }

    public static Element createElementNamedGraph(Table table) {
        Element elData = tableToElement(table);

        ElementTriplesBlock elTriples = new ElementTriplesBlock();
        elTriples.addTriple(Triple.create(Vars.s, Vars.p, Vars.o));

        ElementGroup result = new ElementGroup();
        result.addElement(elData);
        result.addElement(new ElementNamedGraph(Vars.g, elTriples));

        return result;
    }


    public static void addToTables(Tables tables, Quad quad) {
        Node g = quad.getGraph();

        Table defaultGraphTable = tables.getDefaultGraphTable();
        Table namedGraphTable = tables.getNamedGraphTable();

        if(Quad.defaultGraphNodeGenerated.equals(g)) {
            Triple triple = quad.asTriple();
            Binding binding = TripleUtils.tripleToBinding(triple);
            defaultGraphTable.addBinding(binding);
        } else {
            Binding binding = QuadUtils.quadToBinding(quad);
            namedGraphTable.addBinding(binding);
        }

    }

    public static void addToTables(Tables tables, Iterator<Quad> it) {
        while(it.hasNext()) {
            Quad quad = it.next();

            addToTables(tables, quad);
        }
    }

    public static Tables createTablesForQuads(Iterator<Quad> it)
    {
        Tables result = new Tables();
        addToTables(result, it);
        return result;
    }

}
