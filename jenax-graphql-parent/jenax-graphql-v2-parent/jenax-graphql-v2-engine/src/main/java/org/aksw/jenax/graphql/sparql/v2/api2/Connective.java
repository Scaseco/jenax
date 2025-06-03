package org.aksw.jenax.graphql.sparql.v2.api2;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.aksw.jenax.graphql.sparql.v2.util.ElementUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.PatternVars;

public class Connective
    extends BasicConnectInfo
    implements ConnectiveNode
    // extends SelectionSet // Connective is not a SelectionSet - Field is.
{
    /** The graph pattern. */
    protected final Element element;

    /** The variables of the given element which to join on the parent variables. */
    protected final List<Var> connectVars;

    // Cached attributes
    protected final Op op;

    public Connective(Element element, List<Var> connectVars, List<Var> defaultTargetVars, Op op, Set<Var> visibleVars) {
        super(defaultTargetVars, visibleVars);
        this.element = element;
        this.connectVars = connectVars;
        this.op = op;
    }

    public Element getElement() {
        return element;
    }

    public List<Var> getConnectVars() {
        return connectVars;
    }

    /** Create a new connective (copy) where nodes have been remapped accordingly. */
    public Connective applyNodeTransform(NodeTransform nodeTransform) {
        Connective result = Connective.newBuilder()
            .element(ElementUtils.applyNodeTransform(element, nodeTransform))
            .targetVars(defaultTargetVars == null ? null : defaultTargetVars.stream()
                    .map(v -> (Var)nodeTransform.apply(v))
                    .toList())
            .connectVars(connectVars.stream() // Should never be null (I think)
                    .map(v -> (Var)nodeTransform.apply(v))
                    .toList())
            .build();
        return result;
    }

    @Override
    public <T> T accept(ConnectiveVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public String toString() {
        String result = ConnectiveVisitorToString.toString(this);
        return result;
    }

    public boolean isEmpty() {
        return element instanceof ElementGroup g
            ? g.isEmpty() && connectVars != null && connectVars.isEmpty() && defaultTargetVars != null && defaultTargetVars.isEmpty()
            : false;
    }

    /** Visible vars + variables mentioned in the pattern. */
    public Set<Var> getMentionedVars() {
        Set<Var> result = new LinkedHashSet<>(visibleVars);
        PatternVars.vars(result, element);
        return result;
    }

    public static ConnectiveBuilder<?> newBuilder() {
        return new ConnectiveBuilder<>();
    }

    public static Connective of(Path path) {
        return Connective.newBuilder().step(path).build();
    }

    public static Connective empty() {
        return Connective.newBuilder()
                .connectVarNames()
                .targetVarNames()
                .element(new ElementGroup())
                .build();
    }

    public static Connective of(List<Var> sourceVars, List<Var> targetVars, Element elt) {
        return Connective.newBuilder()
            .connectVars(sourceVars)
            .targetVars(targetVars)
            .element(elt)
            .build();
    }
}
