package org.aksw.jenax.graphql.sparql.v2.api2;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.aksw.jenax.graphql.sparql.v2.util.ElementUtils;
import org.aksw.jenax.graphql.sparql.v2.util.Vars;
import org.apache.jena.atlas.lib.SetUtils;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.lang.ParserSPARQL12;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.syntax.Element;

public class ConnectiveBuilder<T extends ConnectiveBuilder<T>>
    implements HasTargetVarBuilder<T>, HasConnectVarBuilder<T>, SparqlPathTraversable<T>
{
    /** The graph pattern. */
    protected Element element;

    /** The variables of the given element which to join on the parent variables. */
    protected List<Var> connectVars;
    protected List<Var> defaultTargetVars;

    /** FIXME Implement allowed vars so it can be declared which variables can be referenced. */
    // protected List<Var> allowedVars;

    public T element(String elementStr) {
        Element elt = ParserSPARQL12.parseElement(elementStr);
        this.element = elt;
        return self();
    }

    public T element(Element element) {
        Objects.requireNonNull(element);
        this.element = element;
        return self();
    }

    @Override
    public void setConnectVars(List<Var> connectVars) {
        this.connectVars = connectVars;
    }

    @Override
    public void setTargetVars(List<Var> targetVars) {
        this.defaultTargetVars = targetVars;
    }

    @Override
    public T step(Path path) {
        Element elt;
        if (path instanceof P_Path0 p0) {
            elt = ElementUtils.createElementTriple(Vars.s, p0.getNode(), Vars.o, p0.isForward());
        } else {
            elt = ElementUtils.createElementPath(Vars.s, path, Vars.o);
        }

        element(elt);

        connectVars(Vars.s);
        targetVars(Vars.o);
        return self();
    }

    public Connective build() {
        Objects.requireNonNull(element);
        Objects.requireNonNull(connectVars);

        // Connect vars can be empty - an independent field that does not join on any variable with the binding.
//        if (connectVars.isEmpty()) {
//            throw new RuntimeException("Connect vars was empty. Cannot connect an element without any connect vars.");
//        }

        // Check for correct variable usage
        Op op = Algebra.compile(element);
        Set<Var> visibleVars = VarHelper.visibleVars(op);

        // TODO Check specifically whether non-visible mentioned variables are referenced in order to
        //   make the error message for specific.
        Set<Var> absentConnectVars = SetUtils.difference(new HashSet<>(connectVars), visibleVars);
        if (!absentConnectVars.isEmpty()) {
            throw new RuntimeException("The connectVars " + absentConnectVars + " are not present or visible in the pattern: " + element);
        }

        Set<Var> absentTargetVars = SetUtils.difference(new HashSet<>(defaultTargetVars), visibleVars);
        if (!absentTargetVars.isEmpty()) {
            throw new RuntimeException("The targetVars " + absentTargetVars + " are not present or visible in the pattern: " + element);
        }

        return new Connective(element, connectVars, defaultTargetVars, op, visibleVars);
    }
}
