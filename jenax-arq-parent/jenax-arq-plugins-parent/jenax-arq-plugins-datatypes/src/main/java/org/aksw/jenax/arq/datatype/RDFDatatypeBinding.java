package org.aksw.jenax.arq.datatype;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * A datatype for storing SPARQL expressions in RDF literals.
 *
 * @author Claus Stadler
 *
 */
public class RDFDatatypeBinding
    extends BaseDatatype
{
    public static final String IRI = "http://jsa.aksw.org/dt/sparql/binding";
    public static final RDFDatatypeBinding INSTANCE = new RDFDatatypeBinding();

    public static RDFDatatype get() {
        return INSTANCE;
    }

    public RDFDatatypeBinding() {
        this(IRI);
    }

    public RDFDatatypeBinding(String uri) {
        super(uri);
    }

    @Override
    public Class<?> getJavaClass() {
        return Binding.class;
    }

    /**
     * Convert a value of this datatype out
     * to lexical form.
     */
    @Override
    public String unparse(Object value) {
        String result = value instanceof Binding
                ? unparse((Binding)value)
                : null;

        return result;
    }

    public static String unparse(Binding binding) {
        ExprList el = new ExprList();
        binding.forEach((v, n) -> {
            el.add(new E_Equals(new ExprVar(v), NodeValue.makeNode(n)));
        });

        String result = RDFDatatypeExprList.unparse(el);
        return result;
    }

    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    @Override
    public Binding parse(String lexicalForm) throws DatatypeFormatException {
        return parseCore(lexicalForm, null);
    }

    public static Binding parseCore(String lexicalForm, Binding parent) {
        BindingBuilder builder = BindingFactory.builder(parent);
        ExprList el = RDFDatatypeExprList.parse(lexicalForm);
        for (Expr e : el) {
            E_Equals x = (E_Equals)e;
            Var v = x.getArg1().asVar();
            Node n = x.getArg2().getConstant().asNode();

            builder.add(v,  n);
        }
        Binding result = builder.build();
        return result;
    }

    public static Binding extractBinding(Node node) {
        Object o = node.getLiteralValue();
        Binding result = o instanceof Binding ? (Binding)o : null;
        return result;
    }

}