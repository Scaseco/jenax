package org.aksw.jenax.sparql.fragment.impl;

import java.util.Arrays;
import java.util.List;

import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

public class FragmentImpl
    implements Fragment
{
    protected Element element;
    protected List<Var> vars;

    public FragmentImpl(Element element, List<Var> vars) {
        super();
        this.element = element;
        this.vars = vars;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public List<Var> getVars() {
        return vars;
    }

    public void setVars(List<Var> vars) {
        this.vars = vars;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((element == null) ? 0 : element.hashCode());
        result = prime * result + ((vars == null) ? 0 : vars.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FragmentImpl other = (FragmentImpl) obj;
        if (element == null) {
            if (other.element != null)
                return false;
        } else if (!element.equals(other.element))
            return false;
        if (vars == null) {
            if (other.vars != null)
                return false;
        } else if (!vars.equals(other.vars))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "RelationImpl [element=" + element + ", vars=" + vars + "]";
    }

    public static Fragment create(Element element, Var ... vars) {
        return new FragmentImpl(element, Arrays.asList(vars));
    }

}
