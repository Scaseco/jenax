package org.aksw.jenax.sparql.fragment.impl;

import java.util.Arrays;
import java.util.List;

import org.aksw.jenax.sparql.fragment.api.Fragment3;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

/**
 * A ternary relation - it can e.g. be joined with a triple pattern
 *
 * @author raven Mar 7, 2018
 *
 */
public class Fragment3Impl
    implements Fragment3
{
    protected Var s;
    protected Var p;
    protected Var o;

    protected Element element;

    public Fragment3Impl(Element element, Var s, Var p, Var o) {
        super();
        this.s = s;
        this.p = p;
        this.o = o;
        this.element = element;
    }

    public List<Var> getVars() {
        return Arrays.asList(s, p, o);
    }

    public Var getS() {
        return s;
    }

    public Var getP() {
        return p;
    }

    public Var getO() {
        return o;
    }

    public Element getElement() {
        return element;
    }

//	public TernaryRelation filterP(Concept concept) {
//
//	}
    @Override
    public String toString() {
        return "TernaryRelation [s=" + s + ", p=" + p + ", o=" + o + ", element=" + element + "]";
    }
}
