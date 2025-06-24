package org.aksw.jenax.graphql.sparql.v2.api2;

import java.util.List;
import java.util.Map;

import org.apache.jena.sparql.core.Var;

public class FragmentSpread
    implements Selection, SelectionBuilder
{
    public Fragment getFragment() {
        return null;
    }

    public Map<Var, Var> getFragmentToInput() {
        return null;
        // return fragmentToInput;
    }


    @Override
    public <T> T accept(ConnectiveVisitor<T> visitor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getBaseName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Connective getConnective() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Var> getParentVars() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SelectionBuilder clone(String finalName, List<Var> parentVars) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Selection build() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

}
//    // protected final SelectionSet parent;
//
//    protected final String name;
//    protected final Fragment fragment;
//
//    /** Variable mapping that defines for each declared variable of the fragment a corresponding one in the connective. */
//    protected final Map<Var, Var> fragmentToInput;
//
//    protected FragmentSpread(SelectionSetBuilder parent, String name, Fragment fragment, Map<Var, Var> fragmentToInput) {
//        // super(parent);
//        // this.parent = parent;
//        this.name = name;
//        this.fragment = fragment;
//        this.fragmentToInput = fragmentToInput;
//    }
//
//    @Override
//    public String getName() {
//        return name;
//    }
//
////    @Override
////    public SelectionSet getParent() {
////        return parent;
////    }
//
//    public Fragment getFragment() {
//        return fragment;
//    }
//
//    public Map<Var, Var> getFragmentToInput() {
//        return fragmentToInput;
//    }
//
//    @Override
//    public Selection build() {
//        return this;
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
//    @Override
//    public String getBaseName() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public Connective getConnective() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public List<Var> getParentVars() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public SelectionBuilder clone(String finalName, List<Var> parentVars) {
//        throw new UnsupportedOperationException("not implemented");
//    }
//
//    @Override
//    public SelectionBuilder toBuilder(SelectionSetBuilder<?> selectionSetBuilder) {
//        throw new UnsupportedOperationException("not implemented");
//    }
//}
