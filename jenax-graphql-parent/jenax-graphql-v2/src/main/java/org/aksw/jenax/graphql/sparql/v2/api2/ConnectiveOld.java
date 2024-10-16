package org.aksw.jenax.graphql.sparql.v2.api2;

//public class ConnectiveOld
//    implements ConnectiveNode
//{
//    /** The graph pattern. */
//    protected final Element element;
//
//    /** The variables of the given element which to join on the parent variables. */
//    protected final List<Var> connectVars;
//
//    /** The default target variables of this element. If a sub field's parentVars is not set then these variables will be used instead. */
//    protected final List<Var> defaultTargetVars;
//    // protected final Map<String, List<Var>> targetVarSets;
//
//    // Cached attributes
//    protected final Op op;
//    protected final Set<Var> visibleVars;
//
//    /** Children can be modified. */
//    protected List<Field> fields;
//
//    public List<Field> getFields() {
//        return fields == null ? Collections.emptyList() : fields;
//    }
//
//    List<Field> getOrSetFields() {
//        if (fields == null) {
//            fields = new ArrayList<>();
//        }
//        return fields;
//    }
//
//    public ConnectiveOld(Element element, List<Var> connectVars, List<Var> defaultTargetVars, Op op, Set<Var> visibleVars) {
//        super();
//        this.element = element;
//        this.connectVars = connectVars;
//        this.defaultTargetVars = defaultTargetVars;
//        this.op = op;
//        this.visibleVars = visibleVars;
//    }
//
//    public Element getElement() {
//        return element;
//    }
//
//    public List<Var> getConnectVars() {
//        return connectVars;
//    }
//
//    public List<Var> getDefaultTargetVars() {
//        return defaultTargetVars;
//    }
//
//    @Override
//    public <T> T accept(ConnectiveVisitor<T> visitor) {
//        T result = visitor.visit(this);
//        return result;
//    }
//
//    @Override
//    public String toString() {
//        String result = ConnectiveVisitorToString.toString(this);
//        return result;
//    }
//
//    /**
//     * Return a builder for a fields on this connective.
//     * Calling {@link FieldBuilder#build()} returns the new field and also adds it to this connective's field list.
//     */
//    public FieldBuilder newFieldBuilder() {
//        return new FieldBuilder(this);
//    }
//
//
//    public static ConnectiveBuilder<?> newBuilder() {
//        return new ConnectiveBuilder<>();
//    }
//}
