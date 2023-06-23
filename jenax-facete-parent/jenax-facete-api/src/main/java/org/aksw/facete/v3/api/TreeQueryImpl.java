package org.aksw.facete.v3.api;

import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

public class TreeQueryImpl
    implements TreeQuery
{
    protected TreeQueryNode root;

    public TreeQueryImpl() {
        this.root = new TreeQueryNodeImpl(this, null);
    }

    @Override
    public TreeQueryNode root() {
        return root;
    }


    public static void main(String[] args) {
        TreeQuery tq = new TreeQueryImpl();

        TreeQueryNode root = tq.root();

        TreeQueryNode n1 = tq.root().getOrCreateChild(FacetStep.fwd(DCTerms.subject, null));
        TreeQueryNode n2 = tq.root().resolve(FacetPath.newRelativePath().resolve(FacetStep.fwd(DCTerms.subject, "a1")));

        System.out.println("1: " + tq.root());

        n1.chRoot();

        System.out.println("2: " + tq.root());
        System.out.println("3: " + n2.getFacetPath());

        root.chRoot();
        System.out.println("4: " + tq.root());

        FacetConstraints constraints = new FacetConstraints(tq);

        ConstraintApiImpl c = constraints.getFacade(n2);
        ConstraintControl cc = (ConstraintControl)c.eq(RDF.type.asNode()).enabled(true);
        System.out.println(constraints);

        cc.unlink();
        System.out.println(constraints);

        cc.enabled(true);
        System.out.println(constraints);
    }
}



